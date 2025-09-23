import { defineStore } from 'pinia'

let id = 0;

export const useAlertStore = defineStore('alert', {
  state: () => ({
    toasts: [] // [{ id, header, message, type }]
  }),
  actions: {
    addToast(message, header = '', type = 'success', duration = 3000) {
      const newToast = {
        id: id++,
        header,
        message,
        type
      };

      this.toasts.push(newToast);

      // auto remove
      setTimeout(() => {
        this.removeToast(newToast.id);
      }, duration);
    },
    removeToast(id) {
      this.toasts = this.toasts.filter(t => t.id !== id);
    }
  }
});

