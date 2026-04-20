<script setup lang="ts">
import { ref } from 'vue'

const username = ref('')
const password = ref('')
</script>

<template>
  <main class="login-page">
    <section class="login-card" aria-labelledby="login-title">
      <div class="login-copy">
        <p class="login-eyebrow">T-Observer</p>
        <h1 id="login-title">T-Observer</h1>
        <p class="login-subtitle">Local account sign in</p>
        <p class="login-helper">
          Sign in with your local account to continue into the observation workspace.
        </p>
      </div>

      <el-form class="login-form" :model="{ username, password }" label-position="top">
        <el-form-item label="Username">
          <el-input v-model="username" placeholder="Enter username" autocomplete="username" />
        </el-form-item>

        <el-form-item label="Password">
          <el-input
            v-model="password"
            type="password"
            placeholder="Enter password"
            autocomplete="current-password"
            show-password
          />
        </el-form-item>

        <el-button
          class="login-submit"
          type="primary"
          native-type="button"
          data-testid="login-submit"
        >
          Sign In
        </el-button>
      </el-form>
    </section>
  </main>
</template>

<style scoped>
.login-page {
  position: relative;
  display: grid;
  min-height: 100vh;
  place-items: center;
  overflow: hidden;
  padding: 24px;
  background:
    radial-gradient(circle at top left, rgba(96, 165, 250, 0.4), transparent 34%),
    radial-gradient(circle at right center, rgba(59, 130, 246, 0.25), transparent 30%),
    linear-gradient(135deg, #eaf2ff 0%, #f7fbff 46%, #eef6ff 100%);
}

.login-page::before,
.login-page::after {
  content: '';
  position: absolute;
  border-radius: 999px;
  filter: blur(28px);
  pointer-events: none;
}

.login-page::before {
  top: -96px;
  left: -56px;
  width: 220px;
  height: 220px;
  background: rgba(64, 158, 255, 0.22);
}

.login-page::after {
  right: -72px;
  bottom: -96px;
  width: 260px;
  height: 260px;
  background: rgba(147, 197, 253, 0.28);
}

.login-card {
  position: relative;
  z-index: 1;
  width: min(100%, 440px);
  padding: 34px;
  border: 1px solid var(--ui-color-card-border);
  border-radius: calc(var(--ui-component-radius) * 2);
  background: var(--ui-color-card-surface);
  box-shadow:
    0 24px 60px rgba(31, 41, 55, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(24px);
}

.login-copy {
  margin-bottom: 28px;
}

.login-eyebrow {
  margin: 0 0 10px;
  color: var(--ui-color-primary);
  font-size: 0.78rem;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.login-copy h1 {
  margin: 0;
  font-size: 2rem;
  line-height: 1.1;
}

.login-subtitle {
  margin: 10px 0 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--ui-color-text-primary);
}

.login-helper {
  margin: 12px 0 0;
  color: var(--ui-color-text-secondary);
  line-height: 1.6;
}

.login-form {
  display: grid;
  gap: 4px;
}

:deep(.el-form-item) {
  margin-bottom: 18px;
}

:deep(.el-form-item__label) {
  padding: 0 0 8px;
  font-weight: 600;
  color: var(--ui-color-text-primary);
}

:deep(.el-input__wrapper) {
  min-height: 44px;
  padding: 1px 14px;
  border-radius: var(--ui-component-radius);
  background: rgba(255, 255, 255, 0.88);
  box-shadow: inset 0 0 0 1px rgba(64, 158, 255, 0.1);
  transition:
    box-shadow 0.2s ease,
    transform 0.2s ease,
    background-color 0.2s ease;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: inset 0 0 0 1px rgba(64, 158, 255, 0.18);
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow:
    0 0 0 1px rgba(64, 158, 255, 0.55),
    0 0 0 4px rgba(64, 158, 255, 0.12);
}

:deep(.el-input__inner) {
  color: var(--ui-color-text-primary);
}

.login-submit {
  margin-top: 6px;
  width: 100%;
  height: 46px;
  border: none;
  border-radius: var(--ui-component-radius);
  background: linear-gradient(135deg, #409eff 0%, #2f80ed 100%);
  box-shadow: 0 16px 28px rgba(64, 158, 255, 0.24);
  transition:
    transform 0.16s ease,
    box-shadow 0.16s ease,
    filter 0.16s ease;
}

.login-submit:hover {
  transform: translateY(-1px);
  box-shadow: 0 18px 32px rgba(64, 158, 255, 0.3);
  filter: brightness(1.02);
}

.login-submit:active {
  transform: translateY(0);
  box-shadow: 0 12px 22px rgba(64, 158, 255, 0.22);
}

.login-submit:focus-visible {
  outline: 3px solid rgba(64, 158, 255, 0.28);
  outline-offset: 2px;
}

@media (max-width: 480px) {
  .login-page {
    padding: 16px;
  }

  .login-card {
    padding: 24px 20px;
    border-radius: 20px;
  }

  .login-copy h1 {
    font-size: 1.7rem;
  }
}
</style>
