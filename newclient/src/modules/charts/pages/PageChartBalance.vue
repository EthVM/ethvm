<template>
    <v-container grid-list-lg class="mb-0">
        <app-bread-crumbs :new-items="crumbs" />
        <balance-chart-data />
    </v-container>
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator'
import AppBreadCrumbs from '@app/core/components/ui/AppBreadCrumbs.vue'
import BalanceChartData from '@app/modules/charts/handlers/BalanceChartData.vue'
import { Crumb } from '@app/core/components/props'
import { TimeseriesKey } from '@app/modules/charts/models'

@Component({
    components: {
        AppBreadCrumbs,
        BalanceChartData
    }
})
export default class PageChartBalance extends Vue {
    /*
  ===================================================================================
    Computed Values
  ===================================================================================
  */
    get crumbs(): Crumb[] {
        return [
            {
                text: this.$t('charts.name'),
                link: '/charts'
            },
            {
                text: 'Eth2 Deposit Balance'
            }
        ]
    }
}
</script>
