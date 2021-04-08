<template>
    <v-card color="white" flat class="pt-3 pb-3">
        <app-table-title :title="getTitle" :has-pagination="showPagination" :page-type="pageType" page-link="/blocks">
            <template v-if="!isHome" #update>
                <notice-new-block @reload="setPage(0, true)" />
            </template>
            <template v-if="showPagination && !initialLoad" #pagination>
                <app-paginate :total="totalPages" :current-page="currentPage" @newPage="setPage" /> </template
        ></app-table-title>
        <table-blocks :max-items="maxItems" :index="index" :is-loading="loading" :table-message="message" :block-data="blocks" :is-scroll-view="isHome" />
        <v-layout
            v-if="showPagination && !initialLoad"
            :justify-end="$vuetify.breakpoint.mdAndUp"
            :justify-center="$vuetify.breakpoint.smAndDown"
            row
            class="pb-1 pr-3 pl-2"
        >
            <app-paginate :total="totalPages" :current-page="currentPage" @newPage="setPage" />
        </v-layout>
    </v-card>
</template>

<script lang="ts">
import AppTableTitle from '@app/core/components/ui/AppTableTitle.vue'
import TableBlocks from '@app/modules/blocks/components/TableBlocks.vue'
import AppPaginate from '@app/core/components/ui/AppPaginate.vue'
import NoticeNewBlock from '@app/modules/blocks/components/NoticeNewBlock.vue'
import { Component, Prop, Watch, Vue } from 'vue-property-decorator'
import BN from 'bignumber.js'
import { getBlocksArrayByNumber, newBlockTable } from './recentBlocks.graphql'
import { getBlocksArrayByNumber_getBlocksArrayByNumber as TypeBlocks } from './apolloTypes/getBlocksArrayByNumber'
import { ErrorMessageBlock } from '@app/modules/blocks/models/ErrorMessagesForBlock'
import { excpInvariantViolation } from '@app/apollo/exceptions/errorExceptions'

interface BlockMap {
    [key: number]: TypeBlocks
}

@Component({
    components: {
        AppTableTitle,
        AppPaginate,
        NoticeNewBlock,
        TableBlocks
    },
    apollo: {
        getBlocksArrayByNumber: {
            query: getBlocksArrayByNumber,
            variables() {
                return {
                    limit: this.maxItems
                }
            },
            fetchPolicy: 'network-only',
            subscribeToMore: [
                {
                    document: newBlockTable,
                    updateQuery: (previousResult, { subscriptionData }) => {
                        try {
                            if (previousResult && subscriptionData.data.newBlockFeed) {
                                const prevB = previousResult.getBlocksArrayByNumber
                                const newB = subscriptionData.data.newBlockFeed
                                newB.txFail = 0
                                const index = prevB.findIndex(block => block.number === newB.number)
                                if (index != -1) {
                                    prevB[index] = newB
                                    return previousResult
                                }
                                return {
                                    __typename: 'BlockSummary',
                                    getBlocksArrayByNumber: [newB, ...prevB]
                                }
                            }
                        } catch (error) {
                            throw error
                        }
                    }
                }
            ],

            result({ data }) {
                if (data && data.getBlocksArrayByNumber) {
                    this.emitErrorState(false)
                    if (this.initialLoad) {
                        this.startBlock = data.getBlocksArrayByNumber[0].number
                        this.index = 0
                        this.totalPages = Math.ceil(new BN(this.startBlock + 1).div(this.maxItems).toNumber())
                        this.initialLoad = false
                    }
                    if (this.pageType === 'home') {
                        if (this.getBlocksArrayByNumber[0].number - this.getBlocksArrayByNumber[1].number > 1) {
                            this.$apollo.queries.getBlocksArrayByNumber.refetch()
                        }
                    }
                }
            },
            error(error) {
                this.initialLoad = true
                this.emitErrorState(true)
            }
        }
    }
})
export default class RecentBlocks extends Vue {
    /*
    ===================================================================================
      Props
    ===================================================================================
    */

    @Prop(Number) maxItems!: number
    @Prop({ type: String, default: 'home' }) pageType!: string

    /*
    ===================================================================================
     Initial Data
    ===================================================================================
    */

    initialLoad = true
    hasError = false
    syncing?: boolean = false
    getBlocksArrayByNumber!: TypeBlocks
    indexedBlocks: BlockMap = {}
    index = 0
    totalPages = 0
    startBlock!: number

    /*
    ===================================================================================
      Computed
    ===================================================================================
    */

    get blocks(): TypeBlocks | [] {
        if (this.indexedBlocks && this.indexedBlocks[this.index]) {
            return this.indexedBlocks[this.index]
        }
        return []
    }

    get message() {
        return ''
    }

    get getTitle(): string {
        const titles = {
            blocks: this.$i18n.t('block.last'),
            address: this.$i18n.t('block.mined'),
            home: this.$i18n.t('block.last')
        }
        return titles[this.pageType]
    }
    get loading(): boolean {
        if (this.hasError) {
            return true
        }
        if (this.isHome) {
            return this.initialLoad
        }
        return this.$apollo.queries.getBlocksArrayByNumber.loading
    }
    get isHome(): boolean {
        return this.pageType === 'home'
    }
    get currentPage(): number {
        return this.index
    }
    get showPagination(): boolean {
        return !this.initialLoad && !this.isHome && this.startBlock - this.maxItems > 0
    }

    /*
    ===================================================================================
      Methods:
    ===================================================================================
    */
    /**
     * Sets current page, checks if it should reset
     * @param page {Number}
     * @param reset {Boolean}
     */
    async setPage(page: number, reset: boolean = false): Promise<boolean> {
        try {
            this.index = page
            if (reset) {
                this.indexedBlocks = {}
                this.initialLoad = true
                await this.$apollo.queries.getBlocksArrayByNumber.refetch()
            } else {
                const from = this.startBlock - this.maxItems * this.index
                if (from >= 0 && !this.indexedBlocks[this.index]) {
                    await this.$apollo.queries.getBlocksArrayByNumber.fetchMore({
                        variables: {
                            fromBlock: from,
                            limit: this.maxItems
                        },
                        updateQuery: (previousResult, { fetchMoreResult }) => {
                            return fetchMoreResult
                        }
                    })
                }
            }
            return true
        } catch (e) {
            const newE = JSON.stringify(e)
            if (!newE.toLowerCase().includes(excpInvariantViolation)) {
                throw new Error(newE)
            }
            return false
        }
    }
    /**
     * Emits error to Sentry
     * @param val {Boolean}
     */
    emitErrorState(val: boolean): void {
        this.hasError = val
        this.$emit('errorBlocksList', this.hasError, ErrorMessageBlock.list)
    }
    /*
    ===================================================================================
      Watch
    ===================================================================================
    */
    @Watch('getBlocksArrayByNumber', { deep: true })
    onGetBlocksArrayByNumberChanged(val: TypeBlocks, oldVal: TypeBlocks) {
        if (val != oldVal) {
            this.$set(this.indexedBlocks, this.index, val)
        }
    }

    /*
    ===================================================================================
      LifeCycle:
    ===================================================================================
    */
    mounted() {
        if (!this.isHome) {
            this.$apollo.skipAllSubscriptions = true
        }
    }
}
</script>
<style scoped lang="css">
.tx-filter-select-container {
    border: solid 1px #efefef;
    padding-top: 1px;
}
.tx-status {
    min-width: 60px;
}
</style>
