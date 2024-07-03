<template>
  <div style="text-align: center">
    <div style="margin-top: 30px">
      <el-steps :active="active" finish-status="success" align-center>
        <el-step title="Verify email address"></el-step>
        <el-step title="Reset password"></el-step>
      </el-steps>
    </div>
    <div style="margin: 0 20px" v-if="active === 0">
      <div style="margin-top: 80px">
        <div style="font-size: 25px;font-weight: bold">
          Rest password
        </div>
        <div style="font-size: 14px;color: grey">
          Please input your registered email address
        </div>
      </div>
      <div style="margin-top: 50px">
        <el-form :model="form" :rules = "rules" ref = "formRef">
          <el-form-item prop="email">
            <el-input v-model="form.email" type="email" placeholder="Email address">
              <template #prefix>
                <el-icon>
                  <Message/>
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="code">
            <el-input v-model="form.code" :maxlength="6" type="text" placeholder="Please input the verification code">
              <template #prefix>
                <el-icon>
                  <EditPen/>
                </el-icon>
              </template>
            </el-input>
            <el-button style="margin-top: 10px" type="success" @click="validateEmail"
                       :disabled="!isEmailValid || coldTime > 0">
              {{ coldTime > 0 ? ' Please wait' + coldTime + ' seconds' : ' Getting verification code' }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      <el-button style="width: 270px;margin-top: 50px" type="warning" @click="confirmReset" plain>Reset password</el-button>
    </div>
    <div style="margin: 0 20px" v-if="active === 1">
      <div style="font-size: 25px;font-weight: bold;margin-top: 50px">
        Rest password
      </div>
      <div style="font-size: 14px;color: grey">
        Please input your new password
      </div>
      <div style="margin-top: 50px">
        <el-form :model="form" :rules="rules" ref="formRef">
          <el-form-item prop="password">
            <el-input v-model="form.password" :maxlength="16" type="password" placeholder="Password">
              <template #prefix>
                <el-icon>
                  <Lock/>
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password_repeat">
            <el-input v-model="form.repeat_password" :maxlength="16" type="password" placeholder="Repeated password">
              <template #prefix>
                <el-icon>
                  <Lock/>
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
        </el-form>
      </div>
      <el-button style="margin-top: 50px;width: 270px" @click="doReset" type="danger">
        Reset password
      </el-button>
    </div>
  </div>
</template>

<script setup>
import {reactive, ref} from "vue"
import {Message} from "@element-plus/icons-vue";
import router from "@/routers/index.js";
import {ElMessage} from "element-plus";
import {post} from "axios";

const active = ref(0)
const form = reactive({
  email: '',
  code: '',
  password: '',
  repeat_password: ''

})

const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('Please input the password again'))
  } else if (value !== form.password) {
    callback(new Error("The input password is different"))
  } else {
    callback()
  }
}

const rules = {
  email: [
    { required: true, message: 'Please input your email address', trigger: 'blur' },
    {type: 'email', message: 'Please enter a valid email address', trigger: ['blur', 'change']}
  ],
  code: [
    { required: true, message: 'Please enter the obtained verification code', trigger: 'blur' },
  ],
  password: [
    { required: true, message: 'Please input password', trigger: 'blur' },
    { min: 6, max: 16, message: 'The password must be between 6 and 16 characters long', trigger: ['blur'] }
  ],
  password_repeat: [
    { validator: validatePassword, trigger: ['blur', 'change'] },
  ],
}

const formRef = ref()
const isEmailValid = ref(false)
const coldTime = ref(0)

const onValidate = (prop, isValid) => {
  if(prop === 'email')
    isEmailValid.value = isValid
}

const validateEmail = () => {
  coldTime.value = 60
  get(`/api/auth/ask-code?email=${form.email}&type=reset`, () => {
    ElMessage.success(`The verification code has been sent to the email: ${form.email}`)
    const handle = setInterval(() => {
      coldTime.value--
      if(coldTime.value === 0) {
        clearInterval(handle)
      }
    }, 1000)
  }, (message) => {
    ElMessage.warning(message)
    coldTime.value = 0
  })
}

const confirmReset = () => {
  formRef.value.validate((isValid) => {
    if(isValid) {
      post('/api/auth/reset-confirm', {
        email: form.email,
        code: form.code
      }, () => active.value++)
    }
  })
}

const doReset = () => {
  formRef.value.validate((isValid) => {
    if(isValid) {
      post('/api/auth/reset-password', {
        email: form.email,
        code: form.code,
        password: form.password
      }, () => {
        ElMessage.success('Password reset successfully, please log in again')
        router.push('/')
      })
    }
  })
}


</script>

<style scoped>

</style>