<template>
  <div class="room">
    <!-- 台灯光晕：整个页面唯一的"光源" -->
    <div class="lamp-glow" aria-hidden="true"></div>

    <main class="desk">
      <!-- 品牌：桌面的铭牌 -->
      <header class="plate">
        <div class="plate-mark">知</div>
        <h1>知识中枢</h1>
        <p class="plate-sub">KNOWLEDGE HUB · 把每一份资料，放上你的长桌</p>
      </header>

      <!-- 借书卡：两张卡（登录/注册）翻面切换 -->
      <section class="card" :class="{ flip: mode === 'register' }">
        <span class="punch" aria-hidden="true"></span>
        <span class="punch punch-r" aria-hidden="true"></span>

        <nav class="card-tabs" role="tablist">
          <button role="tab" :aria-selected="mode === 'login'" :class="{ on: mode === 'login' }" @click="mode = 'login'">登录</button>
          <button role="tab" :aria-selected="mode === 'register'" :class="{ on: mode === 'register' }" @click="mode = 'register'">注册</button>
        </nav>

        <!-- 登录面 -->
        <form v-if="mode === 'login'" class="card-body" @submit.prevent="doLogin">
          <label class="field">
            <span class="field-label">读者名</span>
            <input v-model.trim="loginForm.username" autocomplete="username" placeholder="用户名" required />
          </label>
          <label class="field">
            <span class="field-label">暗号</span>
            <input v-model="loginForm.password" type="password" autocomplete="current-password" placeholder="密码" required />
          </label>
          <button class="brass-btn" type="submit" :disabled="busy">{{ busy ? '正在开门…' : '进入阅览室' }}</button>
          <p class="hint">登录即视为同意做一个好好整理文件的人</p>
        </form>

        <!-- 注册面 -->
        <form v-else class="card-body" @submit.prevent="doRegister">
          <label class="field">
            <span class="field-label">读者名</span>
            <input v-model.trim="regForm.username" autocomplete="username" placeholder="3-50 位字母、数字或下划线" required />
          </label>
          <label class="field">
            <span class="field-label">暗号</span>
            <input v-model="regForm.password" type="password" autocomplete="new-password" placeholder="6-64 位" required />
          </label>
          <label class="field">
            <span class="field-label">称呼（可选）</span>
            <input v-model.trim="regForm.nickname" placeholder="想被怎么称呼" />
          </label>
          <button class="brass-btn" type="submit" :disabled="busy">{{ busy ? '正在登记…' : '办一张借书卡' }}</button>
          <p class="hint">注册即开户，默认配额 10GB</p>
        </form>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const mode = ref<'login' | 'register'>('login')
const busy = ref(false)
const loginForm = reactive({ username: '', password: '' })
const regForm = reactive({ username: '', password: '', nickname: '' })

async function doLogin() {
  busy.value = true
  try {
    await userStore.login({ ...loginForm })
    router.push('/files')
  } finally {
    busy.value = false
  }
}

async function doRegister() {
  busy.value = true
  try {
    await userStore.login({ username: regForm.username, password: regForm.password })
    router.push('/files')
  } catch {
    // 注册失败（如用户名已存在）时 http 层已提示；回登录面让人直接进门
    mode.value = 'login'
  } finally {
    busy.value = false
  }
}
</script>

<style scoped>
.room {
  min-height: 100vh;
  background:
    radial-gradient(720px 420px at 50% 6%, #F0E9D6 0%, rgba(240, 233, 214, 0) 62%),
    linear-gradient(180deg, #FFFFFF 0%, #F4F2EA 78%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
/* 晨光：一枚极淡的暖光斑 */
.lamp-glow {
  position: fixed;
  top: -140px;
  left: 50%;
  width: 560px;
  height: 560px;
  transform: translateX(-50%);
  background: radial-gradient(closest-side, rgba(176, 141, 74, .10), rgba(176, 141, 74, 0));
  pointer-events: none;
}
.desk { width: min(400px, 92vw); padding: 40px 0 64px; position: relative; }

.plate { text-align: center; color: var(--ink); margin-bottom: 28px; }
.plate-mark {
  width: 44px; height: 44px;
  margin: 0 auto 12px;
  border: 1px solid var(--brass);
  border-radius: 8px;
  display: grid; place-items: center;
  color: var(--brass);
  font-size: 20px; font-weight: 600;
}
.plate h1 { margin: 0; font-size: 24px; letter-spacing: .14em; font-weight: 600; }
.plate-sub { margin: 8px 0 0; font-size: 12px; color: var(--room-text-dim); letter-spacing: .18em; }

/* 借书卡：顶部两枚打孔是卡的现实结构，不是装饰 */
.card {
  position: relative;
  background: var(--paper-bright);
  border: 1px solid var(--paper-edge);
  border-radius: 12px;
  padding: 26px 28px 22px;
  box-shadow: var(--shadow-pop);
}
.punch, .punch-r {
  position: absolute; top: 12px;
  width: 14px; height: 14px; border-radius: 50%;
  background: #F0EEE6;
  box-shadow: inset 0 1px 2px rgba(0,0,0,.12);
}
.punch { left: 22px; } .punch-r { right: 22px; }

.card-tabs { display: flex; gap: 4px; margin-bottom: 20px; border-bottom: 1px solid var(--paper-edge); }
.card-tabs button {
  flex: 1; padding: 10px 0 12px;
  background: none; border: none; cursor: pointer;
  font-size: 14px; color: var(--ink-soft);
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
}
.card-tabs button.on { color: var(--ink); font-weight: 600; border-bottom-color: var(--brass); }

.card-body { display: flex; flex-direction: column; gap: 14px; }
.field { display: flex; flex-direction: column; gap: 6px; }
.field-label { font-size: 12px; color: var(--ink-soft); letter-spacing: .06em; }
.field input {
  height: 40px; padding: 0 12px;
  border: 1px solid var(--paper-edge);
  border-radius: var(--radius-sm);
  background: #FFFFFF;
  font-size: 14px; color: var(--ink);
  caret-color: var(--brass-deep);
  transition: border-color .15s;
}
.field input::placeholder { color: var(--ink-faint); }
.field input:focus { outline: none; border-color: var(--brass); box-shadow: 0 0 0 3px rgba(176, 141, 74, .18); }

.brass-btn {
  height: 42px; margin-top: 4px;
  border: none; border-radius: var(--radius-sm);
  background: linear-gradient(180deg, var(--brass-hi), var(--brass));
  color: #FFF9EC; font-size: 15px; font-weight: 600; letter-spacing: .12em;
  cursor: pointer;
  box-shadow: 0 1px 0 rgba(255,255,255,.35) inset, 0 6px 16px rgba(117, 92, 43, .28);
  transition: filter .15s, transform .05s;
}
.brass-btn:hover:not(:disabled) { filter: brightness(1.06); }
.brass-btn:active:not(:disabled) { transform: translateY(1px); }
.brass-btn:disabled { opacity: .6; cursor: default; }

.hint { margin: 2px 0 0; text-align: center; font-size: 12px; color: var(--ink-faint); }
</style>
