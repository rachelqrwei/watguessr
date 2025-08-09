<template>
  <div class="modal-overlay" v-if="visible">
    <div class="modal-content">

      <button class="close-btn" @click="$emit('close')">×</button>
      <form @submit.prevent="submitSignUp" class="login-form">

        <div class="form-group floating-label">
          <input type="email" id="email" v-model="email" placeholder=" " required/>
          <label for="email">EMAIL</label>
        </div>

        <div class="form-group floating-label">
          <input type="text" id="username" v-model="username" placeholder="" required/>
          <label for="username">USERNAME</label>
        </div>
        <p v-if="username.length < 8" class="input-error">Username has to be more than 8 characters</p>

        <div class="form-group floating-label password-wrapper">
          <input
            :type="showPassword ? 'text' : 'password'"
            id="password"
            v-model="password"
            placeholder=" "
            required
          />
          <label for="password">PASSWORD</label>

          <span class="toggle-eye" @click="showPassword = !showPassword">
              <font-awesome-icon :icon="showPassword ? 'eye-slash' : 'eye'" />
          </span>

          <ul class="password-checklist">
            <li :class="{ valid: passwordChecks.lengthValid }">
              {{ passwordChecks.lengthValid ? "✓" : "✗" }} At least 8 characters
            </li>
            <li :class="{ valid: passwordChecks.casingValid }">
              {{ passwordChecks.casingValid ? "✓" : "✗" }} Includes uppercase and lowercase
            </li>
            <li :class="{ valid: passwordChecks.specialCharValid }">
              {{ passwordChecks.specialCharValid ? "✓" : "✗" }} Includes special character (!@#$%^&*)
            </li>
          </ul>
        </div>


        <div class="confirm-password floating-label">
          <input type="password" id="confirmPassword" v-model="confirmPassword" placeholder="" required/>
          <label for="confirmPassword">CONFIRM PASSWORD</label>
        </div>

        <p v-if="error" class="error-message">{{ error }}</p>

        <p v-if="showSuccess" class="success-message">{{ successMessage }}</p>

<!--        <button :disabled="loading" class="login-btn">-->
<!--          <span v-if="loading">Signing up...</span>-->
<!--          <span v-else>Sign Up</span>-->
<!--        </button>-->
<!--        -->

        <StatusModal
          v-if="showStatus"
          :message="statusMessage"
          :type="statusType"
          @close="showStatus = false"
        />

        <OptModal
          :visible="showOtpModal"
          :email="userEmail"
          @close="showOtpModal = false"
          @verified="handleOtpVerified"
        />

        <button :disabled="loading || !passwordChecks.allValid" class="login-btn">
          <span v-if="loading">Signing up...</span>
          <span v-else>SIGN UP</span>
        </button>

        <div class="sign-up">
          <label>Already a Watguessr?
            <span class="link" @click="$emit('openLogin')">LOGIN</span>
          </label>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { useUserStore } from '@/stores/entity/user.ts';
import StatusModal from "@/views/auth/StatusModal.vue";
import OtpModal from "@/views/auth/OtpModal.vue";

export default {
  props: ['visible'],
  components: {
    OptModal: OtpModal,
    StatusModal,
  },
  data() {
    return {
      email: '',
      username: '',
      password: '',
      confirmPassword: '',
      error: '',
      loading: false,
      successMessage: '',
      showSuccess: false,
      statusMessage: '',
      showStatus: false,
      statusType: 'success', // or 'error'
      userStore: useUserStore(),
      showPassword: false
    };
  },
  methods: {
    async submitSignUp() {
      this.error = '';
      if (this.password !== this.confirmPassword) {
        this.error = "Passwords do not match";
        return;
      }
      const { email, username, password } = this;
      this.showSuccess = false;

      try {
        const result = await this.userStore.signUpUser(email, username, password);

        this.statusMessage = "Login successful!";
        this.statusType = "success";
        this.showStatus = true;

        this.successMessage = result;
        this.showSuccess = true;

        this.$emit('openLogin');
        this.$emit('close');

      } catch (err) {
        this.error = err instanceof Error ? err.message : 'Signup failed';
      }
    },
  },
  computed: {
    passwordChecks() {
      const lengthValid = this.password.length >= 8;
      const casingValid = /(?=.*[a-z])(?=.*[A-Z])/.test(this.password);
      const specialCharValid = /(?=.*[!@#$%^&*])/.test(this.password);
      return {
        lengthValid,
        casingValid,
        specialCharValid,
        allValid: lengthValid && casingValid && specialCharValid
      };
    },
  },
  watch: {
    visible(val) {
      if (val) {
        this.error = '';
      }
    }
  },
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
}

.modal-content {
  background-color: #2b2b2b;
  padding: 3rem;
  border-radius: 10px;
  width: 400px;
  color: #fff;
  font-family: 'Segoe UI', sans-serif;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.4);
  position: relative;
}

.close-btn {
  position: absolute;
  top: 0.4rem;
  right: 0.7rem;
  font-size: 1.2rem;
  border: none;
  background: transparent;
  color: #ccc;
  cursor: pointer;
}

.login-form .form-group {
  display: flex;
  flex-direction: column;
  margin-top: 1rem;
  margin-bottom: 0;
}

.login-form .confirm-password {
  display: flex;
  flex-direction: column;
  margin-top: 0;
}

.login-form label {
  font-size: 0.85rem;
  margin-bottom: 0.3rem;
  color: #ccc;
}

.login-form input {
  padding: 0.6rem;
  font-size: 1rem;
  background-color: #3a3a3a;
  border: 1px solid #555;
  color: #eee;
  border-radius: 6px;
  transition: border-color 0.2s ease, background-color 0.2s ease;
}

.login-form input:focus {
  outline: none;
  border-color: #00d8ff;
  background-color: #444;
}

.login-btn {
  padding: 0.6rem 1.2rem;
  background-color: #00d8ff;
  color: black;
  font-weight: bold;
  font-size: 0.95rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  display: block;
  margin: 1rem auto 0;
  margin-top: 3rem
}

.login-btn:hover {
  background-color: #00c4e4;
}

.sign-up {
  margin-top: 1rem;
  text-align: center;
}

.sign-up .link {
  color: var(--yellow);
  text-decoration: none;
  font-weight: 700;
  cursor: pointer;
}

.sign-up .link:hover {
  text-decoration: underline;
}

.error-message {
  color: #ff6b6b;
  font-size: 0.85rem;
  margin-top: 0.25rem;
  margin-bottom: 1rem;
  line-height: 1.4;
  white-space: pre-line;
}

.success-message {
  background-color: #ffe066; /* soft yellow */
  color: #333;
  font-size: 0.85rem;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  text-align: center;
  margin-top: 0.5rem;
  animation: fadeOut 1s ease-in 1s forwards;
}

@keyframes fadeOut {
  to {
    opacity: 0;
  }
}
.input-error {
  color: #e57373;
  font-size: 0.875rem; /* slightly smaller text */
  margin-top: 0;
  display: block;
}
input {
  margin-bottom: 0.1rem; /* or remove this if it's adding too much space */
}
button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transition: opacity 0.3s ease;
}

.password-checklist {
  margin-top: 0.5rem;
  margin-bottom: 1rem;
  list-style: none;
  padding-left: 0;
  font-size: 0.85rem;
  color: #ccc;
}

.password-checklist li {
  margin-bottom: 0.25rem;
}

.password-checklist li.valid {
  color: #00ffae;
}

.password-wrapper {
  position: relative;
}

.password-wrapper input {
  width: 100%;
  padding-right: 2.5rem; /* ensures space for the 👁️ */
}

.toggle-eye {
  position: absolute;
  right: 10px;
  top: 17%;
  transform: translateY(-50%);
  cursor: pointer;
  color: #ccc;
  font-size: 1rem;
  user-select: none;
  z-index: 3;
}

.floating-label {
  position: relative;
  margin-top: 1.5rem;
}

.floating-label input {
  padding: 1.1rem 0.6rem 0.4rem;
  height: 3rem; /* Ensure consistent height */
  background: #3a3a3a;
  border: 1px solid #555;
  border-radius: 6px;
  color: #aaa;
  font-size: 1rem;
}

.floating-label label {
  position: absolute;
  top: 0.9rem;
  left: 0.75rem;
  color: #cccccc;
  font-size: 1rem;
  pointer-events: none;
  background-color: transparent;
  transition: all 0.2s ease;
  padding: 0 0.25rem;
}

.floating-label input:focus + label,
.floating-label input:not(:placeholder-shown) + label,
.floating-label input:valid + label {
  top: 0.1rem;
  left: 0.2rem;
  font-size: 0.6rem;
  color: #aaa;
  padding: 0 0.4rem;
  z-index: 2;
}

.floating-label input:focus {
  border-color: #aaa;
  box-shadow: 0 0 0 1px #aaa;
}

.floating-label input:-webkit-autofill {
  background-color: #3a3a3a !important;
  color: #aaa !important;
}
</style>
