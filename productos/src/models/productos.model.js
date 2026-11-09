const {DataTypes} = require('sequelize')
const sequelizeDB = require('../config/database')

const Producto = sequelizeDB.define('Producto', {

    id:{
        type: DataTypes.INTEGER,
        primaryKey: true,
        autoIncrement: true
    },
    nombre:{
        type: DataTypes.STRING(100),
        allowNull: false
    },
    descripcion:{
        type: DataTypes.TEXT,
        allowNull: true
    },
    precio:{
        type: DataTypes.DECIMAL(10,2),
        allowNull: false,
        validate: {
            isDecimal: true,
            min: 0.01
        }
    },
    stock:{
        type: DataTypes.INTEGER,
        allowNull: false,
        defaultValue:0,
        validate:{
            min:0
        }
    }
},{
    tableName: 'productos',
    timestamps: false
})

    module.exports = Producto