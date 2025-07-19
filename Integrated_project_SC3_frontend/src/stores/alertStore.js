import { defineStore } from 'pinia'

export const useAlertStore = defineStore('alert', {
  state: () => ({
    message: '',
    type: 'success',
  }),
  actions: {
    setMessage(msg, type = 'success') {
      this.message = msg
      this.type = type
      setTimeout(() => {
        this.clearMessage();
      }, 3000);
    },
    clearMessage() {
      this.message = ''
      this.type = 'success'
    }
  }
})
