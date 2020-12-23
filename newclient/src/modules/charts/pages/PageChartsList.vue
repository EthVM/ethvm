<template>
    <v-container grid-list-lg class="mb-0">
        <app-bread-crumbs :new-items="crumbs"></app-bread-crumbs>
        <app-tabs :tabs="pageTabs">
            <!--
            =====================================================================================
             TXS TAB
            =====================================================================================
            -->
            <v-tab-item slot="tabs-item" value="tab-0" class="tabdiv">
                <v-layout :class="contentClass" row wrap justify-start>
                    <v-flex xs12 md6>
                        <v-container class="block-grey">
                            <v-layout row align-center>
                                <v-flex xs8 sm7 md8 pb-0 pr-0>
                                    <v-card-title class="title font-weight-bold pl-0">View Eth2 Balance Change</v-card-title>
                                    <v-card-text class="info--text pt-0 pl-0 caption">View Eth2 Balance Change By Day, Hour, Minute.</v-card-text>
                                    <button class="btn-details" @click="rerouteBalance">{{ $t('btn.details') }}</button>
                                </v-flex>
                                <v-flex xs4 sm5 md4>
                                    <v-img :src="require('@/assets/graph-icon.png')" contain height="100"></v-img>
                                </v-flex>
                            </v-layout>
                        </v-container>
                    </v-flex>
                </v-layout>
            </v-tab-item>

            <v-tab-item slot="tabs-item" value="tab-1" class="tabdiv">
                <v-layout :class="contentClass" row wrap justify-start>
                    <v-flex xs12 md6>
                        <chart-link :chart-id="ChartType.average_txs.link" />
                    </v-flex>
                    <v-flex xs12 md6>
                        <chart-link :chart-id="ChartType.total_txs.link" />
                    </v-flex>
                    <v-flex xs12 md6>
                        <chart-link :chart-id="ChartType.min_txs.link" />
                    </v-flex>
                    <v-flex xs12 md6>
                        <chart-link :chart-id="ChartType.max_txs.link" />
                    </v-flex>
                </v-layout>
            </v-tab-item>
            <!--
            =====================================================================================
             PENDING TXS TAB
            =====================================================================================
            -->
            <v-tab-item slot="tabs-item" value="tab-2" class="tabdiv">
                <v-layout :class="contentClass" row wrap justify-start>
                    <v-flex xs12 md6>
                        <chart-link :chart-id="ChartType.average_new_pending_txs.link" />
                    </v-flex>
                    <v-flex xs12 md6>
                        <chart-link :chart-id="ChartType.total_new_pending_txs.link" />
                    </v-flex>
                </v-layout>
            </v-tab-item>
            <!--
            =====================================================================================
            GAS PRICE TAB
            =====================================================================================
            -->
            <v-tab-item slot="tabs-item" value="tab-3" class="tabdiv">
                <v-layout :class="contentClass" row wrap justify-start>
                    <v-flex xs12 md6>
                        <chart-link :chart-id="ChartType.average_gas_price.link" />
                    </v-flex>
                    <v-flex xs12 md6>
                        <chart-link :chart-id="ChartType.min_gas_price.link" />
                    </v-flex>
                    <v-flex xs12 md6>
                        <chart-link :chart-id="ChartType.max_gas_price.link" />
                    </v-flex>
                </v-layout>
            </v-tab-item>
        </app-tabs>
    </v-container>
</template>

<script lang="ts">
import AppTabs from '@app/core/components/ui/AppTabs.vue'
import AppBreadCrumbs from '@app/core/components/ui/AppBreadCrumbs.vue'
import ChartLink from '@app/modules/charts/components/ChartLink.vue'
import { Tab, Crumb } from '@app/core/components/props'
import { Vue, Component } from 'vue-property-decorator'
import { ChartRouteKey } from '@app/modules/charts/models'

@Component({
    components: {
        AppBreadCrumbs,
        AppTabs,
        ChartLink
    }
})
export default class PageChartsList extends Vue {
    /*
  ===================================================================================
    Initial Data
  ===================================================================================
  */
    ChartType = ChartRouteKey

    /*
  ===================================================================================
    Computed
  ===================================================================================
  */

    get crumbs(): Crumb[] {
        return [
            {
                text: this.$t('charts.name')
            }
        ]
    }

    get pageTabs(): Tab[] {
        return [
            {
                id: 0,
                title: 'Balance',
                isActive: true
            },
            {
                id: 1,
                title: this.$i18n.tc('tx.name', 2),
                isActive: false
            },
            {
                id: 2,
                title: this.$i18n.tc('tx.pending', 2),
                isActive: false
            },
            {
                id: 3,
                title: this.$i18n.t('gas.price'),
                isActive: false
            }
        ]
    }

    get contentClass(): String {
        return this.$vuetify.breakpoint.name === 'xs' ? 'ma-0' : 'ma-2'
    }

    rerouteBalance(): void {
        this.$router.push({ path: 'eth2-balance' })
    }
}
</script>
<style scoped lang="scss">
.tabdiv {
    min-height: 490px;
}
.block-grey {
    border: 1px solid #b4bfd2;
}

.btn-details {
    border: 1px solid #6270fc;
    color: #6270fc;
    border-radius: 2px;
    padding: 10px 20px;
}
</style>
