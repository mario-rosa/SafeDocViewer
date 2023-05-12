const crypto = require('crypto')
module.exports = {
    parsePublicKey: function (publicKey) {
        const pK = publicKey
        .replace("-----BEGIN PUBLIC KEY-----", "")
        .replace("-----END PUBLIC KEY-----", "")
        .replace("\n", "")
        .trim()
        return pK
    },
    parsePrivateKey: function (privateKey) {
        const pK = privateKey
        .replace("-----BEGIN PRIVATE KEY-----", "")
        .replace("-----END PRIVATE KEY-----", "")
        .replace("\n", "")
        .trim()
        return pK
    },
    generateJsonKey: function (securitykey, initVector) {
        let data = {
            "securitykey": securitykey,
            "initVector": initVector,
            "encoding" : "base64"
        }
        return JSON.stringify(data)
    },
    generateJsonMessage: function (message, publicKey) {
        let data = {
            "message": message,
            "publicKey": publicKey
        }
        return JSON.stringify(data)
    },
    generateJsonPdf: function (name, path, pdf, password, expires) {
        let data = {
            "name": name,
            "path": path,
            "pdf": pdf,
            "password": password,
            "expires": expires
        }
        return JSON.stringify(data)
    },
    encriptMessage: function (message, key, iv) {
        var encrypt = crypto.createCipheriv('aes-256-cbc', key, iv)
        var encrypted = encrypt.update(message, 'utf8', 'base64')
        encrypted += encrypt.final('base64')
        return encrypted
    },
    decriptMessage: function (message, key, iv) {
        var decrypt = crypto.createDecipheriv('aes-256-cbc', key, iv)
        var decrypted = decrypt.update(message, 'base64', 'utf8')
        decrypted += decrypt.final()
        return decrypted
    },
    randomPassword: function(length){
        charset = "!#$%&()*+,-./:;<=>?@[\]^_{|}~0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ!#$%&()*+,-./:;<=>?@[\]^_`{|}~0123456789abcdefghijklmnopqrstuvwxyz",
        password = ""
        for (var i = 0, n = charset.length; i < length; i++) {
            password += charset.charAt(Math.floor(Math.random() * n))
        }
        return password
    },
    currentDatePlusDays: function(numDays){
        var date = new Date();
        date.setDate(date.getDate() + numDays)
        return date.getTime()
    }
}