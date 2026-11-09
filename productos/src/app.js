//app.js
const express = require('express')
const productoRoutes = require('./routes/productos.routes')

const app = express()
app.use(express.json())
app.use('/api', productoRoutes)

module.exports = app