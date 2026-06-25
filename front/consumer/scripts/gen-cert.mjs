import { execSync } from 'node:child_process'
import { existsSync, mkdirSync } from 'node:fs'
import { networkInterfaces } from 'node:os'
import { join, dirname } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const certDir = join(__dirname, '..', '.cert')
const caKey = join(certDir, 'ca.key')
const caCert = join(certDir, 'ca.crt')
const serverKey = join(certDir, 'cert.key')
const serverCert = join(certDir, 'cert.crt')

// ensure .cert dir
if (!existsSync(certDir)) {
  mkdirSync(certDir, { recursive: true })
}

// get local IP
function getLocalIP() {
  const nics = networkInterfaces()
  for (const name of Object.keys(nics)) {
    for (const net of nics[name] || []) {
      if (net.family === 'IPv4' && !net.internal) {
        return net.address
      }
    }
  }
  return null
}

const localIP = getLocalIP()
const domains = ['localhost', '127.0.0.1']
if (localIP) domains.push(localIP)

console.log(`domains: ${domains.join(', ')}`)

const mkcert = join(__dirname, '..', 'node_modules', '.bin', 'mkcert')

// create CA if not exists
if (!existsSync(caKey) || !existsSync(caCert)) {
  console.log('creating CA...')
  execSync(
    `"${mkcert}" create-ca --key "${caKey}" --cert "${caCert}" --organization "Dev" --country-code CN`,
    { stdio: 'inherit' }
  )
}

// create server cert
console.log('creating server cert...')
execSync(
  `"${mkcert}" create-cert --ca-key "${caKey}" --ca-cert "${caCert}" --key "${serverKey}" --cert "${serverCert}" --domains ${domains.join(' ')}`,
  { stdio: 'inherit' }
)

console.log('cert ready')
