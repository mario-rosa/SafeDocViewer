const { PDFDocument } = require('pdf-lib')
const express = require("express")
const app = express()
const { MongoClient } = require("mongodb")
const fs = require("fs")
const session = require("express-session")
var bodyParser = require("body-parser")
var qpdf = require("node-qpdf")
const crypto = require("crypto")
const utility = require("./utility")

var client = null
db = null
const { publicKey, privateKey } = crypto.generateKeyPairSync("rsa", {
  modulusLength: 2048,
  publicKeyEncoding: {
    type: "spki",
    format: "pem",
    padding: crypto.constants.RSA_PKCS1_OAEP_PADDING,
  },
  privateKeyEncoding: {
    type: "pkcs8",
    format: "pem",
    padding: crypto.constants.RSA_PKCS1_OAEP_PADDING,
  },
})
const initVector = crypto.randomBytes(16)
const securitykey = crypto.randomBytes(32)

app.use(
  bodyParser.urlencoded({
    extended: false,
  })
)

app.use(
  session({
    secret: "your_secret_key",
    resave: true,
    saveUninitialized: true,
  })
)

async function connectMongo() {
  client = new MongoClient("mongodb://127.0.0.1:27017")
  await client.connect()
  console.log("Connected successfully to server")
  db = client.db("serverTesi")
}

app.get("/getPdf/:id", async (req, res) => {
  if (req.session.user == null) {
    res.statusMessage = "Forbidden"
    res.status(403).end()
    return
  }
  const ids = parseInt(req.params.id)

  const filteredDocs = await db.collection("pdf").find({ ref: ids }).toArray()
  if (filteredDocs.length == 0) {
    res.statusMessage = "Resource not found"
    res.status(404).end()
    return
  }

  const presente = await db.collection("userPdf").find({ idUser: req.session.user._id , idPdf: ids }).toArray();
  if(presente.length != 0){
    res.statusMessage = "Resource alredy requested"
    res.status(404).end()
    return
  } else { 
    var myobj = { idUser: req.session.user._id, idPdf: ids };
    await db.collection("userPdf").insertOne(myobj);
  }
  const path = filteredDocs[0]["path"] // path del file pdf sul server
  const newPath = "enc\\" + path.substring(4) // path del file pdf con data mod
  const encPath = "enc\\" + path.substring(4,path.length-4) + "Enc" + req.session.user._id + ".pdf" // path del file pdf encripted
  const password = utility.randomPassword(6)
  const options = {
    keyLength: 256,
    password: password,
    outputFile: encPath,
  }
  var expDate = ""
  if(req.session.user.privileges == 2){
    expDate = utility.currentDatePlusDays(14)
  }else if(req.session.user.privileges == 3){
    expDate = utility.currentDatePlusDays(7)
  }
  const exPdfBytes = fs.readFileSync(path)

  const pdfDoc = await PDFDocument.load(exPdfBytes, {updateMetadata: false})
  pdfDoc.setCreationDate(new Date())
 
  /*-----aggiunge scadenza ma solo per adobe-----
  var rightNow = new Date();
  var day = rightNow.getDate();
  var month = rightNow.getMonth();
  var year = rightNow.getFullYear();
  
  pdfDoc.addJavaScript( 
    'main',
   `function expire(){var year=${year}; var month=${month};var day=${day};today = new Date();expiry = new Date(year, month, day);if (today.getTime() > expiry){app.alert("The file is expired. You need a new one.");closeDoc();}}expire();`,
); */
  
  const pdfBytes = await pdfDoc.save()
  fs.writeFileSync(newPath, pdfBytes)

  await qpdf.encrypt(newPath, options)
  fs.unlinkSync(newPath)

  fs.readFile(encPath, function (err, data) {
    if (err) {
      res.statusMessage = "Error"
      res.status(500).end()
      return
    }
    const pdfJson = utility.generateJsonPdf(filteredDocs[0]["name"], path, data.toString("base64"), password, expDate)
    const enc = utility.encriptMessage(pdfJson, securitykey, initVector)

    res.send(enc)
    fs.unlinkSync(encPath)
  })
})

app.get("/getAllPdf", async (req, res) => {
  if (req.session.user == null) {
    res.statusMessage = "Forbidden"
    res.status(403).end()
    return
  }

  if (req.session.user.privileges == 3) {
    const findResult = await db.collection("pdf").find({"type": "base"}, {projection: { _id: 0}}).toArray();
    const enc = utility.encriptMessage(
      JSON.stringify(findResult),
      securitykey,
      initVector
    );
    res.send(enc)

  } else {
    const findResult = await db.collection("pdf").find({},{projection: { _id: 0}}).toArray();
    const enc = utility.encriptMessage(
      JSON.stringify(findResult),
      securitykey,
      initVector 
    );
    res.send(enc)
  //const dec = utility.decriptMessage(enc, key)
  }  
})
app.post("/getKey", async (req, res) => {
  var clientPublicKey = req.body.clientPublicKey

  if (req.session.user == null) {
    res.statusMessage = "Forbidden"
    res.status(403).end()
    return
  }
  if (clientPublicKey == null || clientPublicKey == "") {
    res.statusMessage = "Bad request"
    res.status(400).end()
    return
  }

  const encrypted1 = crypto.privateEncrypt(
    privateKey,
    Buffer.from(
      utility.generateJsonKey(
        securitykey.toString("base64"),
        initVector.toString("base64")
      )
    )
  )
  const encrypted2 = crypto.publicEncrypt(
    clientPublicKey,
    Buffer.from(encrypted1)
  )
  message = encrypted2.toString("base64")
  res.send(utility.generateJsonMessage(message, publicKey))
})

app.post("/login", async (req, res) => {
  var username = req.body.username
  var password = req.body.password

  if (username == null || password == null) {
    res.statusMessage = "Bad request"
    res.status(400).end()
    return
  }

  var user = await db.collection("users").findOne({
    username: username,
  })

  if (user == null) {
    res.statusMessage = "User non found"
    res.status(403).end()
    return
  }

  if (password == user.password) {
    req.session.user = user
    res.send(user)
  } else {
    res.statusMessage = "Current password does not match"
    res.status(403).end()
  }
  return
})

app.get("/logout", async (req, res) => {
  req.session.destroy()
  res.statusMessage = "Disconnected"
  res.status(200).end()
})

app.get("/", (req, res) => {
  res.send("Server running")
})

app.listen(3000, () => {
  console.log(`Example app listening at http://localhost:3000`)

  connectMongo()
})
