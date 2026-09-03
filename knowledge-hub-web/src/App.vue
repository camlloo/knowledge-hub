<template>
  <router-view />
</template>

<style>
/* 全局基础字体与底色走设计令牌（见 styles/tokens.css） */
body {
  margin: 0;
  font-family: var(--font-sans);
  background: var(--room-deep);
  color: var(--ink);
  -webkit-font-smoothing: antialiased;
}
</style>
