import httpx
from fastapi import APIRouter, Depends, HTTPException, Request, Response
from app.config import config
from app.dependencies import usuario_actual

router = APIRouter(tags=["Gateway / Proxy"])

METODOS = ["GET", "POST", "PUT", "PATCH", "DELETE"]

# --- PROXY PARA SERVICIO DE PRODUCTOS (NestJS) ---
@router.api_route("/productos", methods=METODOS)
@router.api_route("/productos/{ruta:path}", methods=METODOS)
async def proxy_productos(
    request: Request, ruta: str = "", usuario = Depends(usuario_actual)
):
    # Si la ruta es vacía apunta a /api/productos, si trae algo (ej. "producto" o "producto/1") respeta la ruta exacta
    if ruta:
        destino = f"{config.url_ms_productos}/api/{ruta}"
    else:
        destino = f"{config.url_ms_productos}/api/productos"

    return await reenviar_peticion(request, destino, usuario)

# --- PROXY PARA SERVICIO DE PEDIDOS (Spring Boot) ---
@router.api_route("/pedidos", methods=METODOS)
@router.api_route("/pedidos/{ruta:path}", methods=METODOS)
async def proxy_pedidos(
    request: Request, ruta: str = "", usuario = Depends(usuario_actual)
):
    destino = f"{config.url_ms_pedidos}/pedido"
    if ruta:
        destino = f"{config.url_ms_pedidos}/pedido/{ruta}"

    return await reenviar_peticion(request, destino, usuario)


# --- FUNCIÓN AUXILIAR DE REENVÍO (EVITA DUPLICAR CÓDIGO) ---
async def reenviar_peticion(request: Request, destino: str, usuario: dict):
    cabeceras = {"X-Usuario": usuario["usuario"], "X-Rol": usuario["rol"]}
    if "content-type" in request.headers:
        cabeceras["Content-Type"] = request.headers["content-type"]

    try:
        async with httpx.AsyncClient(timeout=15) as cliente:
            respuesta = await cliente.request(
                request.method,
                destino,
                params=request.query_params,
                content=await request.body(),
                headers=cabeceras
            )
    except httpx.ConnectError:
        raise HTTPException(status_code=502, detail="El microservicio de destino no está disponible")
    except httpx.TimeoutException:
        raise HTTPException(status_code=504, detail="El microservicio tardó demasiado en responder")

    return Response(
        content=respuesta.content,
        status_code=respuesta.status_code,
        media_type=respuesta.headers.get("content-type")
    )