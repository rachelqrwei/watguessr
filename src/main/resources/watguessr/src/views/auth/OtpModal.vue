<template>
  <Transition name="modal-fade">
    <div v-if="visible" class="modal-overlay" @click.self="$emit('close')">
      <div class="modal-content" role="dialog" aria-modal="true" aria-labelledby="otp-title">
      <button class="close-btn" @click="$emit('close')" aria-label="Close">×</button>

      <!-- OTP Input State -->
        <h2 id="otp-title" class="title">Enter Verification Code</h2>
        <p class="subtitle">We sent a 6‑digit code to <strong>{{ email }}</strong>.</p>

        <div class="form-row">
          <input
            type="text"
            inputmode="numeric"
            pattern="[0-9]*"
            v-model="otp"
            maxlength="6"
            placeholder="123456"
            class="otp-input"
            @keyup.enter="submitOtp"
            autofocus
          />
          <button class="verify-btn" :disabled="submitting || otp.length !== 6" @click="submitOtp">
            {{ submitting ? 'Verifying…' : 'Verify' }}
          </button>
        </div>

<!--        Success Message-->
        <p v-if="showSuccess" class="success-message">Account Verified!</p>

        <div class="helper-row">
          <button class="link-btn" :disabled="cooldown > 0" @click="$emit('resend')">
            {{ cooldown > 0 ? `Resend in ${cooldown}s` : 'Resend code' }}
          </button>
        </div>

        <p v-if="error" class="error">{{ error }}</p>
    </div>
  </div>
  </Transition>


</template>

<script>
import { mapActions } from 'vuex';

export default {
  name: 'OtpModal',
  props: {
    visible: { type: Boolean, default: false },
    email: { type: String, required: true },
    username: { type:String, required: true},
    password: { type:String, required: true}
  },
  data() {
    return {
      otp: '',
      error: null,
      success: false,
      submitting: false,
      cooldown: 0,
      redirectCountdown: 3,
      _iv: null,
      _redirectIv: null,
      showSuccess: '',
    };
  },
  methods: {
    ...mapActions('user', ['verifyOtp', 'login']),

    async submitOtp() {
      if (this.submitting) return;
      this.error = null;
      this.submitting = true;
      try {
        const res = await this.verifyOtp({ email: this.email, submittedOtp: this.otp });

        if (res === 'verified') {
          this.success = true;
          // success message
          this.showSuccess = true;

                    // login in user
          const resLogin = await this.login({username: this.username, password: this.password});

          // Show welcome modal for successful new user signup
          if (resLogin) {
            // Use store to trigger welcome modal on home page
            this.$store.commit('user/SHOW_WELCOME_MODAL');
          }

          this.$emit('verified');
        } else {
          this.error = res || 'Verification failed.';
        }
      } catch (e) {
        this.error = 'Network error. Try again.';
      } finally {
        this.submitting = false;
      }
    },

    startRedirectCountdown() {
      this.redirectCountdown = 1;
      this._redirectIv = setInterval(() => {
        if (this.redirectCountdown > 0) {
          this.redirectCountdown--;
        } else {
          clearInterval(this._redirectIv);
          this.$emit('close');
        }
      }, 1000);
    }
  },
  mounted() {
    const T = 30;
    this.cooldown = T;
    this._iv = setInterval(() => {
      if (this.cooldown > 0) this.cooldown--;
    }, 1000);
  },
  beforeUnmount() {
    clearInterval(this._iv);
    if (this._redirectIv) {
      clearInterval(this._redirectIv);
    }
  }
};
</script>


<style scoped>
/* overlay */
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,.55);
  backdrop-filter: blur(2px);
  display: grid; place-items: center;
  padding: 16px;
  z-index: 1000;
}

/* card */
.modal-content {
  width: 100%;
  max-width: 420px;
  background: #1f1f1f;
  color: #f5f5f5;
  border-radius: 16px;
  padding: 24px 20px 20px;
  box-shadow: 0 12px 40px rgba(0,0,0,.5);
  position: relative;
}

/* header */
.title {
  margin: 6px 0 4px;
  font-size: 1.25rem; font-weight: 700;
  text-align: center;
}
.subtitle {
  margin: 0 0 16px;
  opacity: .8; text-align: center; font-size: .95rem;
}

/* close */
.close-btn {
  position: absolute; top: 10px; right: 12px;
  width: 32px; height: 32px; border-radius: 50%;
  border: 0; background: transparent; color: #cfcfcf;
  font-size: 22px; line-height: 1; cursor: pointer;
}
.close-btn:hover { color: #fff; }

/* form */
.form-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
  align-items: center;
}

.otp-input {
  height: 44px; padding: 0 14px; border-radius: 10px;
  border: 1px solid #3a3a3a; background: #2a2a2a; color: #fff;
  font-size: 1.05rem; letter-spacing: .12em; text-align: center;
  outline: none; transition: border-color .15s;
}
.otp-input:focus { border-color: #00d8ff; }

.verify-btn {
  height: 44px; padding: 0 16px;
  border: 0; border-radius: 10px; cursor: pointer;
  background: #00d8ff; color: #111; font-weight: 700;
  transition: transform .06s ease, filter .2s ease;
}
.verify-btn:disabled { opacity: .6; cursor: not-allowed; }
.verify-btn:active { transform: translateY(1px); }

/* helpers */
.helper-row { margin-top: 10px; text-align: center; }
.link-btn {
  background: transparent; border: 0; color: #9ad7ff;
  cursor: pointer; font-size: .95rem; padding: 6px 8px;
}
.link-btn:disabled { opacity: .5; cursor: not-allowed; }

/* messages */
.error   { margin-top: 12px; color: #FF7F7F; text-align: center; }
.success { margin-top: 12px; color: #B6FF7F; text-align: center; }

/* Success State */
.success-state {
  text-align: center;
  padding: 20px 0;
}

@keyframes successPulse {
  0% {
    transform: scale(0.8);
    opacity: 0;
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
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

</style>
