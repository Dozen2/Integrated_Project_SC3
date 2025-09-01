// import { defineStore } from 'pinia'

// export const useAlertStore = defineStore('alert', {
//   state: () => ({
//     header: '',
//     message: '',
//     type: 'success',
//   }),
//   actions: {
//     setMessage(msg,head, type = 'success') {
//       this.header = head
//       this.message = msg
//       this.type = type
//       setTimeout(() => {
//         this.clearMessage();
//       }, 3000);
//     },
//     clearMessage() {
//       this.header = ''
//       this.message = ''
//       this.type = 'success'
//     }
//   }
// })

// stores/alert.js
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

