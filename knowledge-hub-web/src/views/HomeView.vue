<template>
  <div class="home">
    <el-card class="card" shadow="never">
      <template #header>
        <div class="header">
          <h2>知识中枢 · Knowledge Hub</h2>
          <el-tag type="success">项目骨架就绪</el-tag>
        </div>
      </template>

      <el-descriptions title="环境连接" :column="1" border>
        <el-descriptions-item label="后端">Spring Boot 3.5 · JDK 21 → http://localhost:8080/api</el-descriptions-item>
        <el-descriptions-item label="MySQL">192.168.150.101:3306 / knowledge_hub</el-descriptions-item>
        <el-descriptions-item label="Redis">192.168.150.101:6379</el-descriptions-item>
        <el-descriptions-item label="MinIO">192.168.150.101:9000（控制台 :9001）</el-descriptions-item>
      </el-descriptions>

      <el-divider />

      <p class="tip">骨架已初始化（无业务代码）。从阶段①「文件管理」开始，按
        <b>auth → user → folder → file → tag → storage</b> 的顺序实现各模块。</p>
      <el-button type="primary" @click="sayReady">Element Plus 可用性检查</el-button>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'

function sayReady() {
  ElMessage.success('前端骨架运行正常')
}
</script>

<style scoped>
.home {
  display: flex;
  justify-content: center;
  padding-top: 10vh;
}
.card {
  width: 640px;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.header h2 {
  margin: 0;
}
.tip {
  color: #909399;
}
</style>
