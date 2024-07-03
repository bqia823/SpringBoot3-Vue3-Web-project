<script setup>
import {EditPen, Lock, Message, User} from "@element-plus/icons-vue";
// import router from "@/router";
import router from "@/routers/index.js";
import {reactive, ref} from "vue";
import {ElMessage} from "element-plus";
import {get, post} from "@/net";

const form = reactive({
  username: '',
  password: '',
  password_repeat: '',
  email: '',
  code: ''
})

const validateUsername = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('Please input an username'))
  } else if (!/^[a-zA-Z0-9\u4e00-\u9fa5]+$/.test(value)) {
    callback(new Error('The username cannot contain special characters'))
  } else {
    callback()
  }
}

const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('Please input password again'))
  } else if (value !== form.password) {
    callback(new Error("The input password is different"))
  } else {
    callback()
  }
}

const rules = {
  username: [
    {validator: validateUsername, trigger: ['blur', 'change']},
    {min: 2, max: 8, message: 'The length of username must be between 2 and 8 characters long', trigger: ['blur', 'change']},
  ],
  password: [
    {required: true, message: 'Please input password', trigger: 'blur'},
    {min: 6, max: 16, message: 'The length of the password must be between 6-16 characters', trigger: ['blur', 'change']}
  ],
  password_repeat: [
    {validator: validatePassword, trigger: ['blur', 'change']},
  ],
  email: [
    {required: true, message: 'Please input your email address', trigger: 'blur'},
    {type: 'email', message: 'Please enter a legal E-mail address', trigger: ['blur', 'change']}
  ],
  code: [
    {required: true, message: 'Please enter the verification code', trigger: 'blur'},
  ]
}

const formRef = ref()
const isEmailValid = ref(false)
const coldTime = ref(0)

const onValidate = (prop, isValid) => {
  if (prop === 'email')
    isEmailValid.value = isValid
}

const register = () => {
  formRef.value.validate((isValid) => {
    if (isValid) {
      post('/api/auth/register', {
        username: form.username,
        password: form.password,
        email: form.email,
        code: form.code
      }, () => {
        ElMessage.success('Register successfully, welcome to join us')
        router.push("/")
      })
    } else {
      ElMessage.warning('Please complete the registration form')
    }
  })
}

const validateEmail = () => {
  coldTime.value = 60
  get(`/api/auth/ask-code?email=${form.email}&type=register`, () => {
    ElMessage.success(`The verification code has been sent to your email: ${form.email}，please remember to check`)
    const handle = setInterval(() => {
      coldTime.value--
      if (coldTime.value === 0) {
        clearInterval(handle)
      }
    }, 1000)
  }, undefined, (message) => {
    ElMessage.warning(message)
    coldTime.value = 0
  })
}
</script>

<template>
  <div style="text-align: center;margin: 0 20px">
    <div style="margin-top: 100px">
      <div style="font-size: 25px;font-weight: bold">New user register</div>
      <div style="font-size: 14px;color: grey">Welcome to register for our platform, please fill in the relevant information below</div>
    </div>
    <div style="margin-top: 50px">
      <el-form :model="form" :rules="rules" @validate="onValidate" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" :maxlength="8" type="text" placeholder="Username">
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" :maxlength="16" type="password" placeholder="Password">
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password_repeat">
          <el-input v-model="form.password_repeat" :maxlength="16" type="password" placeholder="Repeated password">
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="form.email" type="email" placeholder="Email address">
            <template #prefix>
              <el-icon><Message /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="code">
              <el-input v-model="form.code" :maxlength="6" type="text" placeholder="Please input the verification code">
                <template #prefix>
                  <el-icon><EditPen /></el-icon>
                </template>
              </el-input>
              <el-button style="margin-top: 10px" type="success" @click="validateEmail"
                         :disabled="!isEmailValid || coldTime > 0">
                {{coldTime > 0 ? ' Please wait' + coldTime + ' seconds' : ' Getting verification code'}}
              </el-button>
        </el-form-item>
      </el-form>
    </div>
    <div style="margin-top: 80px">
      <el-button style="width: 270px" type="warning" @click="register" plain>Register now</el-button>
    </div>
    <div style="margin-top: 20px">
      <span style="font-size: 14px;line-height: 15px;color: grey">Already have an account? </span>
      <el-link type="primary" style=" translate: 0 -2px" @click="router.push('/')">Login now</el-link>
    </div>
  </div>
</template>

<style scoped>

</style>