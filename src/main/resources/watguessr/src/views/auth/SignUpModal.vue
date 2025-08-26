<template>
  <Transition name="modal-fade">
    <div v-if="visible" class="modal-overlay" @click="$emit('close')">
      <div class="modal-content" @click.stop>

      <button class="close-btn" @click="$emit('close')">×</button>

      <!-- Google Sign In Button -->
      <div class="google-signin-section">
        <button @click="handleGoogleSignIn" class="google-signin-btn" :disabled="loading">
          <div class="google-btn-content">
            <svg class="google-icon" viewBox="0 0 24 24" aria-hidden="true">
              <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
              <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
              <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
              <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
            </svg>
            <span class="google-btn-text">Continue with Google</span>
          </div>
        </button>

        <div class="divider">
          <span>or</span>
        </div>
      </div>

      <form @submit.prevent="submitSignUp" class="login-form">

        <div class="form-group floating-label">
          <input type="email" id="email" v-model="email" placeholder=" " required/>
          <label for="email">EMAIL</label>
        </div>

        <div class="form-group floating-label">
          <input type="text" id="username" v-model="username" placeholder="" required/>
          <label for="username">USERNAME</label>
        </div>
        <p v-if="username.length < 3" class="input-error">Username has to be more than 3 characters</p>

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


        <button type="submit" :disabled="loading || !passwordChecks.allValid" class="login-btn">
          <div v-if="loading">Signing up...</div>
          <div v-else>SIGN UP</div>
        </button>

        <div class="sign-up">
          <label>Already have an account?
            <span class="link" @click="$emit('openLogin')">LOG IN</span>
          </label>
        </div>
      </form>

      <OtpModal
        :visible="showOtpModal"
        :email="userEmail"
        @close="showOtpModal = false"
        @verified="handleOtpVerified"
        @resend="resendOtp"
      />
    </div>
  </div>
  </Transition>
</template>
<script lang="ts">
import { mapGetters, mapActions } from 'vuex';
import OtpModal from "@/views/auth/OtpModal.vue";

export default {
  props: ['visible'],
  components: {
    OtpModal
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
      showPassword: false,
      userEmail: '',
      showOtpModal: false
    };
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
        allValid: lengthValid && casingValid && specialCharValid,
      };
    },
  },
  methods: {
    ...mapActions('user', ['signUpUser', 'sendOtp', 'startGoogleAuth']),

    async submitSignUp() {
      this.error = '';
      if (this.password !== this.confirmPassword) {
        this.error = "Passwords do not match";
        return;
      }
      const { email, username, password } = this;
      this.showSuccess = false;
      this.loading = true;

      try {
        const result = await this.signUpUser({ email, username, password });

        this.successMessage = "Account created successfully! Please check your email for verification.";
        this.showSuccess = true;

        // call OTP Modal
        this.userEmail = email;
        // OTP is automatically sent by backend after signup, no need to send again
        this.showOtpModal = true;

      } catch (err) {
        const rawMessage = err instanceof Error ? err.message : 'Signup failed';
        let friendly = rawMessage;
        if (/user_email_address_key|duplicate key.*email|email.*exists/i.test(rawMessage)) {
          friendly = 'An account with this email already exists. Please log in or use a different email.';
        } else if (/user_username_key|username.*exists/i.test(rawMessage)) {
          friendly = 'Username already taken. Please choose another one.';
        } else if (/Password does not meet criteria/i.test(rawMessage)) {
          friendly = 'Password must be at least 8 characters and include uppercase, lowercase, and a special character.';
        }
        this.error = friendly;
      } finally {
        this.loading = false;
      }
    },
    handleOtpVerified() {
      this.showOtpModal = false;
      this.$emit('openLogin');
    },
    async resendOtp() {
      await this.sendOtp(this.userEmail);
    },

    async handleGoogleSignIn() {
      try {
        this.loading = true;
        await this.startGoogleAuth();
      } catch (err) {
        this.error = 'Failed to start Google authentication';
        this.loading = false;
      }
    }
  },
  watch: {
    visible(val) {
      if (val) {
        this.error = '';
      }
    }
  }
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
  background: rgba(42, 42, 44, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(8px);
  padding: 3rem 2rem;
  border-radius: 10px;
  width: 380px;
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
  padding: 14px 28px;
  background: rgba(255, 235, 59, 0.15);
  color: var(--yellow);
  border: 0.5px solid var(--yellow);
  font-weight: bold !important;
  font-size: 0.95rem;
  border-radius: 10px;
  cursor: pointer;
  display: block;
  margin: 1rem auto 0;
  margin-top: 1.25rem;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(8px);
  box-shadow: 0 4px 15px rgba(255, 235, 59, 0.1);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.login-btn::before {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.login-btn:hover::before {
  left: 100%;
}

.login-btn div {
  font-weight: bold !important;
}

.login-btn:hover {
  background: rgba(255, 235, 59, 0.2);
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(255, 235, 59, 0.2);
  border-color: rgba(255, 235, 59, 0.8);
}

.login-btn:active {
  transform: translateY(-1px);
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
  color: #FF7F7F;
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
  color: #FF7F7F;
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
  color: #B6FF7F;
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
  top: -0.2rem;
  left: 0.2rem;
  font-family: "Red Hat Text", sans-serif;
  font-style: normal;
  font-weight: 400;
  font-size: 0.6rem;
  letter-spacing: 0.7px;
  color: var(--light-grey);
  line-height: 1.6;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  padding: 0.3rem 0.4rem;
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

/* Google Sign In Styles */
.google-signin-section {
  margin-bottom: 1.5rem;
}

.google-signin-btn {
  width: 100%;
  padding: 14px 20px;
  background: #ffffff;
  color: #3c4043;
  border: 1px solid #dadce0;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 500;
  font-family: 'Google Sans', 'Roboto', 'Segoe UI', sans-serif;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 3px rgba(60, 64, 67, 0.12), 0 1px 2px rgba(60, 64, 67, 0.24);
  position: relative;
  overflow: hidden;
}

.google-signin-btn:hover {
  background: #f8f9fa;
  box-shadow: 0 2px 8px rgba(60, 64, 67, 0.16), 0 2px 4px rgba(60, 64, 67, 0.28);
  border-color: #c4c7c5;
  transform: translateY(-1px);
}

.google-signin-btn:active {
  background: #f1f3f4;
  transform: translateY(0);
  box-shadow: 0 1px 3px rgba(60, 64, 67, 0.12), 0 1px 2px rgba(60, 64, 67, 0.24);
}

.google-signin-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: 0 1px 3px rgba(60, 64, 67, 0.12), 0 1px 2px rgba(60, 64, 67, 0.24);
}

.google-btn-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  width: 100%;
}

.google-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.google-btn-text {
  font-weight: 500;
  letter-spacing: 0.25px;
  white-space: nowrap;
}

.divider {
  text-align: center;
  margin: 1.5rem 0;
  position: relative;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(to right, transparent, rgba(255, 255, 255, 0.3), transparent);
}

.divider span {
  background: rgba(42, 42, 44, 0.95);
  padding: 0 1.5rem;
  color: #a0a0a0;
  font-size: 0.85rem;
  font-weight: 500;
  position: relative;
  z-index: 1;
  letter-spacing: 0.5px;
}

/* Mobile Responsive Styles */
@media (max-width: 768px) {
  .modal-content {
    width: 90vw;
    max-width: 340px;
    padding: 2rem 1.5rem;
    margin: 1rem;
  }

  .google-signin-btn {
    padding: 12px 16px;
    font-size: 0.9rem;
  }

  .google-btn-text {
    font-size: 0.85rem;
  }

  .google-icon {
    width: 18px;
    height: 18px;
  }

  .floating-label input {
    padding: 1rem 0.6rem 0.3rem;
    height: 2.8rem;
    font-size: 0.95rem;
  }

  .floating-label label {
    font-size: 0.95rem;
  }

  .login-btn {
    padding: 12px 24px;
    font-size: 0.9rem;
    margin-top: 1rem;
  }

  .divider {
    margin: 1.25rem 0;
  }

  .divider span {
    font-size: 0.8rem;
    padding: 0 1rem;
  }

  .google-signin-section {
    margin-bottom: 1.25rem;
  }
}

@media (max-width: 480px) {
  .modal-content {
    width: 95vw;
    max-width: 320px;
    padding: 1.5rem 1.25rem;
    margin: 0.5rem;
  }

  .google-signin-btn {
    padding: 10px 14px;
    font-size: 0.85rem;
  }

  .google-btn-text {
    font-size: 0.8rem;
  }

  .google-icon {
    width: 16px;
    height: 16px;
  }

  .floating-label input {
    padding: 0.9rem 0.6rem 0.2rem;
    height: 2.6rem;
    font-size: 0.9rem;
  }

  .floating-label label {
    font-size: 0.9rem;
  }

  .login-btn {
    padding: 10px 20px;
    font-size: 0.85rem;
  }

  .divider {
    margin: 1rem 0;
  }

  .divider span {
    font-size: 0.75rem;
    padding: 0 0.75rem;
  }

  .google-signin-section {
    margin-bottom: 1rem;
  }
}

</style>
