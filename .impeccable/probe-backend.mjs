/** 后端契约探针：验证 auth/user 全链路 + 未实现接口的降级表现 */
const B = 'http://localhost:8080/api'
const U = 'probe' + Date.now()
const j = (r) => r.json()

const out = []
function log(step, body, extra = '') {
  const line = `${step}  code=${body.code}  ${body.message}${extra ? '  ' + extra : ''}`
  out.push(line)
  console.log(line)
}

// 1. 注册（含中文昵称，验证 UTF-8 链路）
const reg = await fetch(`${B}/v1/auth/register`, {
  method: 'POST', headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username: U, password: '123456', nickname: '探针员' }),
}).then(j)
log('1.注册', reg, `nickname=${reg.data?.nickname}`)

// 2. 登录
const login = await fetch(`${B}/v1/auth/login`, {
  method: 'POST', headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username: U, password: '123456' }),
}).then(j)
log('2.登录', login, `expiresIn=${login.data?.expiresIn}`)
let { accessToken: T, refreshToken: R } = login.data

// 3. GET /users/me
const me = await fetch(`${B}/v1/users/me`, { headers: { Authorization: `Bearer ${T}` } }).then(j)
log('3.me', me, `quota=${JSON.stringify(me.data?.quota)}`)

// 4. PUT /users/me 带提权字段
const upd = await fetch(`${B}/v1/users/me`, {
  method: 'PUT', headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${T}` },
  body: JSON.stringify({ nickname: '改名探针', role: 'ADMIN' }),
}).then(j)
log('4.改资料(带role)', upd, `nickname=${upd.data?.user?.nickname} role=${upd.data?.user?.role}(应为USER)`)

// 5. 改密码：旧密码错误
const bad = await fetch(`${B}/v1/users/me/password`, {
  method: 'PUT', headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${T}` },
  body: JSON.stringify({ oldPassword: 'wrong!', newPassword: '654321' }),
}).then(j)
log('5.旧密码错误', bad, '(应1004)')

// 6. 改密码成功 → 全端踢下线
const ok = await fetch(`${B}/v1/users/me/password`, {
  method: 'PUT', headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${T}` },
  body: JSON.stringify({ oldPassword: '123456', newPassword: '654321' }),
}).then(j)
log('6.改密码成功', ok)

// 7. 旧 refreshToken 再刷新（应 1011）
const old = await fetch(`${B}/v1/auth/refresh`, {
  method: 'POST', headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ refreshToken: R }),
}).then(j)
log('7.旧refresh', old, '(应1011)')

// 8. 新密码重新登录 → 轮换：第二次用同一 refresh 应 1011
const l2 = await fetch(`${B}/v1/auth/login`, {
  method: 'POST', headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username: U, password: '654321' }),
}).then(j)
T = l2.data.accessToken; R = l2.data.refreshToken
await fetch(`${B}/v1/auth/refresh`, {
  method: 'POST', headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ refreshToken: R }),
}).then(j)
const reuse = await fetch(`${B}/v1/auth/refresh`, {
  method: 'POST', headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ refreshToken: R }),
}).then(j)
log('8.refresh重放', reuse, '(应1011，轮换防重放)')

// 9. 未实现接口（带有效 token）：应 404 而非 500
const files = await fetch(`${B}/v1/files`, { headers: { Authorization: `Bearer ${T}` } }).then(j)
log('9.GET /files(未实现)', files, '(应404)')

// 10. 无 token → 1010
const no = await fetch(`${B}/v1/users/me`).then(j)
log('10.无token', no, '(应1010)')

// 11. 登录失败（密码错误）→ 1002
const badLogin = await fetch(`${B}/v1/auth/login`, {
  method: 'POST', headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username: U, password: 'wrong-password' }),
}).then(j)
log('11.密码错误登录', badLogin, '(应1002)')
