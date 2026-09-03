<template>
  <el-dialog
    :model-value="modelValue"
    title="读者档案"
    width="480px"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div class="prof paper-scope">
      <!-- 头像圆章 + 基本信息 -->
      <div class="prof-head">
        <span class="seal">{{ firstChar }}</span>
        <div>
          <strong>{{ userStore.me?.user.username }}</strong>
          <p class="dim">角色：{{ userStore.me?.user.role === 'ADMIN' ? '管理员' : '读者' }}</p>
        </div>
      </div>

      <el-form label-width="72px" label-position="left" class="prof-form">
        <el-form-item label="称呼">
          <el-input v-model="form.nickname" maxlength="50" placeholder="想被怎么称呼" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" maxlength="100" placeholder="name@example.com" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="saveProfile">保存资料</el-button>
        </el-form-item>
      </el-form>

      <el-divider />
      <p class="sec-title">换暗号（成功后全端下线）</p>
      <el-form label-width="72px" label-position="left" class="prof-form">
        <el-form-item label="原暗号">
          <el-input v-model="pwd.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新暗号">
          <el-input v-model="pwd.newPassword" type="password" show-password placeholder="6-64 位" />
        </el-form-item>
        <el-form-item>
          <el-button :loading="changing" @click="changePwd">修改密码</el-button>
        </el-form-item>
      </el-form>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { apiUpdateMe, apiUpdatePassword } from '@/api/user'
import { useUserStore } from '@/stores/user'

const props = defineProps<{ modelValue: boolean }>()
const emit = defineEmits<{ 'update:modelValue': [boolean] }>()
const userStore = useUserStore()
const router = useRouter()

const firstChar = computed(() => userStore.nickname.slice(0, 1).toUpperCase())

const form = reactive({ nickname: '', email: '', avatar: '' })
const pwd = reactive({ oldPassword: '', newPassword: '' })
const saving = ref(false)
const changing = ref(false)

watch(() => props.modelValue, (open) => {
  if (open && userStore.me) {
    form.nickname = userStore.me.user.nickname ?? ''
    form.email = userStore.me.user.email ?? ''
    form.avatar = userStore.me.user.avatar ?? ''
    pwd.oldPassword = ''
    pwd.newPassword = ''
  }
})

async function saveProfile() {
  saving.value = true
  try {
    // 白名单更新：只传三个可变字段
    userStore.me = await apiUpdateMe({
      nickname: form.nickname || undefined,
      email: form.email || undefined,
      avatar: form.avatar || undefined,
    })
    ElMessage.success('资料已更新')
  } finally {
    saving.value = false
  }
}

async function changePwd() {
  if (!pwd.oldPassword || !pwd.newPassword) {
    ElMessage.warning('原暗号和新暗号都要填')
    return
  }
  changing.value = true
  try {
    await apiUpdatePassword({ oldPassword: pwd.oldPassword, newPassword: pwd.newPassword })
    ElMessage.success('暗号已换，全部设备将被请出阅览室')
    userStore.forceLogout()
    router.push('/login')
  } finally {
    changing.value = false
  }
}
</script>

<style scoped>
.prof-head { display: flex; align-items: center; gap: 14px; margin-bottom: 18px; }
.seal {
  width: 46px; height: 46px; border-radius: 50%;
  background: var(--room-panel);
  border: 1.5px solid var(--brass);
  color: var(--brass);
  display: grid; place-items: center;
  font-size: 20px; font-weight: 600;
}
.dim { margin: 2px 0 0; font-size: 12px; color: var(--ink-soft); }
.sec-title { margin: 0 0 12px; font-size: 13px; font-weight: 600; color: var(--ink); }
</style>
