const Producto = require('../models/productos.model')
const productoModel = require('../models/productos.model')

async function buscarPorId(req, res) {
    try {
       const {id}=req.params
       const producto = await Producto.findOne({ where: { id, activo: true } })

       if(!producto){
            return res.status(404).json({
                error: 'No existe un producto con id '+id
            })
       }

       res.status(200).json({
            mensaje: 'Producto encontrado',
            producto: producto
       })
    }catch (error){
        console.log(error)
        res.status(500).json({ error: 'Error interno del servidor'})
    }
}

async function buscarTodos(req, res) {
    try {
        const productos = await Producto.findAll({ where: { activo: true } })
        res.status(200).json(productos)
    } catch (error) {
        console.log(error)
        res.status(500).json({ error: 'Error interno del servidor' })
    }
}

async function crearProducto(req, res) {
    const { nombre, descripcion, precio, stock } = req.body

    if (!nombre || precio === undefined || stock === undefined) {
        return res.status(400).json({
            error: 'Faltan datos obligatorios (nombre, precio, stock)'
        })
    }

    if (precio <= 0 || stock < 0) {
        return res.status(400).json({
            error: 'El precio debe ser mayor a 0 y el stock no puede ser negativo'
        })
    }

    try {
        const nuevoProducto = await Producto.create({
            nombre,
            descripcion,
            precio,
            stock
        })

        res.status(201).json({
            mensaje: "Producto creado correctamente",
            producto: nuevoProducto
        })
    } catch (error) {
        console.log(error)
        res.status(500).json({ error: 'Error interno del servidor' })
    }
}

async function actualizarProducto(req, res) {
    const { id } = req.params
    const { nombre, descripcion, precio, stock } = req.body

    if (!nombre || precio === undefined || stock === undefined) {
        return res.status(400).json({
            error: 'Faltan datos obligatorios'
        })
    }

    if (precio <= 0 || stock < 0) {
        return res.status(400).json({
            error: 'Valores de precio o stock inválidos'
        })
    }

    try {
        const producto= await Producto.findOne({ where: { id, activo: true } })

        if (!producto) {
            return res.status(404).json({
                error: 'No encontrado'
            })
        }

        await producto.update({ nombre, descripcion, precio, stock})
        res.status(200).json({ mensaje: 'Producto actualizado' })
    } catch (error) {
        console.error(error)
        res.status(500).json({
            error: 'Error interno del servidor'
        })
    }
}

async function eliminarProducto(req, res) {
    const { id } = req.params
    try {
        const producto = await Producto.findOne({ where: { id, activo: true } })

        if (!producto) {
            return res.status(404).json({ error: 'No encontrado' })
        }

        producto.activo = false
        await producto.save()
        res.status(204).send()
    } catch (error) {
        console.error(error)
        res.status(500).json({ error: 'Error interno del servidor' })
    }
}

async function descontarStock(req, res) {
    const { id } = req.params
    const { cantidad } = req.body

    try {
        const producto = await Producto.findOne({ where: { id, activo: true } })
        if (!producto) {
            return res.status(404).json({ error: 'Producto no encontrado' })
        }

        if (producto.stock < cantidad) {
            return res.status(400).json({ error: 'Stock insuficiente' })
        }

        producto.stock -= cantidad
        await producto.save()

        res.status(200).json({
            mensaje: 'Stock actualizado correctamente',
            producto
        })
    } catch (error) {
        console.log(error)
        res.status(500).json({ error: 'Error interno del servidor' })
    }
}

async function sumarStock(req, res) {
    const { id } = req.params
    const { cantidad } = req.body

    try {
        const producto = await Producto.findByPk(id)
        if (!producto) {
            return res.status(404).json({ error: 'Producto no encontrado' })
        }

        producto.stock += cantidad
        await producto.save()

        res.status(200).json({
            mensaje: 'Stock actualizado correctamente',
            producto
        })
    } catch (error) {
        console.log(error)
        res.status(500).json({ error: 'Error interno del servidor' })
    }
}

module.exports = {
    buscarPorId,
    buscarTodos,
    crearProducto,
    actualizarProducto,
    eliminarProducto,
    descontarStock,
    sumarStock
}