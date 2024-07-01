<script setup>
import { logout } from '@/net'
import router from "@/routers";

function userLogout() {
  logout(() => router.push("/"))
}
</script>

<template>
  <div>
    <el-button @click="userLogout">Logout</el-button>
  </div>
</template>

<style scoped>

</style>
