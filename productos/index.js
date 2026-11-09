//index.js

const app = require('./src/app')
const PUERTO = process.env.PORT || 3001
const sequelize = require('./src/config/database')

async function iniciarAplicacion() {
    try {
        await sequelize.authenticate()
       app.listen(PUERTO, () => console.log(`PROYECTO CORRIENDO EN PUERTO ${PUERTO}`))
    } catch (e) {
        console.error(e)
        console.error("Hay un error: " + e)
    }
}

iniciarAplicacion()