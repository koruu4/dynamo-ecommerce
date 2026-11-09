const express = require('express')
const router = express.Router()
const productoController = require('../controllers/productos.controller')

router.get('/productos', productoController.buscarTodos)
router.get('/producto/:id', productoController.buscarPorId)
router.post('/producto', productoController.crearProducto)
router.put('/producto/:id', productoController.actualizarProducto)
router.delete('/producto/:id', productoController.eliminarProducto)
router.patch('/producto/:id/stock', productoController.descontarStock) 
router.patch('/producto/:id/stock/suma', productoController.sumarStock) 

module.exports = router