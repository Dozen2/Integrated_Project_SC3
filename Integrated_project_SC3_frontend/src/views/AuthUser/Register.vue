<script setup>
import Tabs from "primevue/tabs";
import TabList from "primevue/tablist";
import Tab from "primevue/tab";
import TabPanels from "primevue/tabpanels";
import TabPanel from "primevue/tabpanel";
import RegisterBuyer from "@/components/AuthUserComponent/RegisterBuyer.vue";
import RegisterSeller from "@/components/AuthUserComponent/RegisterSeller.vue";


</script>
<template>
  <div class="card">
    <Tabs value="0">
      <TabList>     
        <Tab value="0">Buyer</Tab>
        <Tab value="1">Seller</Tab>
      </TabList>
      <TabPanels>
        <TabPanel value="0">
          <RegisterBuyer />
        </TabPanel>
        <TabPanel value="1">
          <RegisterSeller />
        </TabPanel>
      </TabPanels>
    </Tabs>
  </div>
</template>

