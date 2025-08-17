import "./assets/main.css";

import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import { createPinia } from "pinia";
import PrimeVue from "primevue/config";
import ToastService from "primevue/toastservice";
// import PrimeVue from 'primevue/config';
import Aura from "@primeuix/themes/aura";

const app = createApp(App);
app.use(PrimeVue, {
  theme: {
    preset: Aura,
    options: {
        darkModeSelector: false || 'none',
    }
  },
});

// const app = createApp(App)
const pinia = createPinia();

app.use(PrimeVue);
app.use(ToastService);
app.use(router);
app.use(pinia);
app.mount("#app");
