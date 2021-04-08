<template>
    <v-card color="white" flat class="pt-3 pb-2">
        <app-table-title :title="getTitle" :has-pagination="showPagination" :page-type="pageType" page-link="/txs">
            <template v-if="!isHome && !isBlock" #update>
                <app-new-update :text="$t('message.update.txs')" :update-count="newMinedTransfers" :hide-count="true" @reload="setPage(0, true)" />
            </template>
            <template v-if="showPagination && !initialLoad" #pagination>
                <app-paginate v-if="isBlock" :total="totalPages" :current-page="index" @newPage="setPage" />
                <app-paginate-has-more v-else :has-more="hasMore" :current-page="index" :loading="loading" @newPage="setPage" /> </template
        ></app-table-title>
        <table-txs :max-items="maxItems" :index="index" :is-loading="loading" :table-message="message" :txs-data="transactions" :is-scroll-view="isHome" />
        <v-layout
            v-if="showPagination && !initialLoad"
            :justify-end="$vuetify.breakpoint.mdAndUp"
            :justify-center="$vuetify.breakpoint.smAndDown"
            row
            class="pb-1 pr-3 pl-2"
        >
            <app-paginate v-if="isBlock" :total="totalPages" :current-page="index" @newPage="setPage" />
            <app-paginate-has-more v-else :has-more="hasMore" :current-page="index" :loading="loading" @newPage="setPage" />
        </v-layout>
    </v-card>
</template>

<script lang="ts">
import AppTableTitle from '@app/core/components/ui/AppTableTitle.vue'
import AppPaginate from '@app/core/components/ui/AppPaginate.vue'
import AppPaginateHasMore from '@app/core/components/ui/AppPaginateHasMore.vue'
import AppNewUpdate from '@app/core/components/ui/AppNewUpdate.vue'
import TableTxs from '@app/modules/txs/components/TableTxs.vue'
import { Component, Prop, Watch, Vue } from 'vue-property-decorator'
import BN from 'bignumber.js'
import { getBlockTransfers, getAllTxs, newTransfersCompleteFeed } from './queryTransfers.graphql'
import {
    getBlockTransfers_getBlockTransfers as TypeBlockTransfers,
    getBlockTransfers_getBlockTransfers_transfers as TypeTransfers
} from './apolloTypes/getBlockTransfers'
import { newTransfersCompleteFeed as TypeTransfersSubscribtion } from './apolloTypes/newTransfersCompleteFeed'
import { getAllTxs_getAllEthTransfers as AllTxType } from './apolloTypes/getAllTxs'
import { TransferType } from '@app/apollo/global/globalTypes'
import { ErrorMessageBlock } from '@app/modules/blocks/models/ErrorMessagesForBlock'
import { excpInvariantViolation } from '@app/apollo/exceptions/errorExceptions'

@Component({
    components: {
        AppTableTitle,
        AppPaginate,
        AppPaginateHasMore,
        AppNewUpdate,
        TableTxs
    },
    apollo: {
        getBlockTransfers: {
            query: getBlockTransfers,
            fetchPolicy: 'network-only',
            skip() {
                return this.skipBlockTxs
            },
            variables() {
                return this.blockRef ? { _number: parseInt(this.blockRef) } : undefined
            },
            result({ data }) {
                if (data && data.getBlockTransfers) {
                    if (data.getBlockTransfers.transfers.length > 0) {
                        this.totalPages = Math.ceil(new BN(data.getBlockTransfers.transfers.length).div(this.maxItems).toNumber())
                    }
                    this.emitErrorState(false)
                    this.initialLoad = false
                }
            },
            error(error) {
                this.emitErrorState(true)
            }
        },
        getAllEthTransfers: {
            query: getAllTxs,
            fetchPolicy: 'network-only',
            variables() {
                return {
                    _limit: this.maxItems,
                    _nextKey: null
                }
            },
            skip() {
                return this.isBlock
            },
            result({ data }) {
                if (data && data.getAllEthTransfers && data.getAllEthTransfers.transfers) {
                    this.initialLoad = false
                    this.emitErrorState(false)
                } else {
                    this.emitErrorState(true)
                }
            },
            error(error) {
                this.emitErrorState(true)
            }
        },
        $subscribe: {
            newTransfersCompleteFeed: {
                query: newTransfersCompleteFeed,

                skip() {
                    return this.isBlock
                },
                result({ data }) {
                    if (data.newTransfersCompleteFeed.type === TransferType.ETH) {
                        if (this.isHome) {
                            this.$apollo.queries.getAllEthTransfers.refresh()
                            this.emitErrorState(false)
                        } else {
                            this.newMinedTransfers++
                        }
                    }
                },
                error(error) {
                    this.emitErrorState(true)
                }
            }
        }
    }
})
export default class BlockTxs extends Vue {
    /*
    ===================================================================================
      Props
    ===================================================================================
    */

    @Prop(Number) maxItems!: number
    @Prop({ type: String, default: 'home' }) pageType!: string
    @Prop(String) blockRef?: string
    @Prop({ type: Boolean, default: false }) isMined!: boolean

    /*
    ===================================================================================
     Initial Data
    ===================================================================================
    */

    initialLoad = true
    getBlockTransfers!: TypeBlockTransfers
    getAllEthTransfers!: AllTxType
    index = 0
    totalPages = 0
    isEnd = 0
    newMinedTransfers = 0
    hasError = false

    /*
    ===================================================================================
      Computed
    ===================================================================================
    */

    get transactions(): (TypeTransfers | null)[] | [] {
        if (this.isBlock && this.getBlockTransfers && this.getBlockTransfers.transfers !== null) {
            return this.getBlockTransfers.transfers
        }
        if (!this.isBlock && this.getAllEthTransfers && this.getAllEthTransfers.transfers !== null) {
            return this.getAllEthTransfers.transfers
        }
        return []
    }

    get message(): string {
        if (this.loading) {
            return ''
        }
        if (this.isBlock && this.transactions.length === 0) {
            return `${this.$t('message.tx.no-in-block')}`
        }
        return ''
    }

    get isHome(): boolean {
        return this.pageType === 'home'
    }

    get isBlock(): boolean {
        return this.pageType === 'blockDetails'
    }
    get skipBlockTxs(): boolean {
        return !(this.isBlock && this.isMined)
    }

    get getTitle(): string {
        return this.isBlock ? `${this.$t('block.txs')}` : `${this.$tc('tx.last', 1)}`
    }
    get showPagination(): boolean {
        if (this.isHome) {
            return false
        }
        if (this.isBlock) {
            return this.totalPages > 1 ? true : false
        }

        return this.hasMore
    }

    get loading(): boolean {
        if (this.hasError) {
            return true
        }
        if (this.isBlock || this.isHome) {
            return this.initialLoad || this.$apollo.queries.getBlockTransfers.loading
        }
        return this.$apollo.queries.getAllEthTransfers.loading
    }
    get hasMore(): boolean {
        if (!this.isHome && !this.isBlock) {
            return this.getAllEthTransfers ? !(this.getAllEthTransfers.nextKey === null) : false
        }
        return false
    }

    /*
    ===================================================================================
      Methods:
    ===================================================================================
    */
    /**
     * Sets page number and reset value and emit
     * @param page {Number}
     * @param reset {Boolean}
     */
    async setPage(page: number, reset: boolean = false): Promise<boolean> {
        try {
            if (reset) {
                this.isEnd = 0
                if (!this.isHome && !this.isBlock) {
                    this.newMinedTransfers = 0
                }
                this.$apollo.queries.getAllEthTransfers.refetch()
            } else {
                if (!this.isBlock && page > this.isEnd && this.hasMore) {
                    this.$apollo.queries.getAllEthTransfers.fetchMore({
                        variables: {
                            _limit: this.maxItems,
                            _nextKey: this.getAllEthTransfers.nextKey
                        },
                        updateQuery: (previousResult, { fetchMoreResult }) => {
                            this.isEnd = page
                            const newT = fetchMoreResult.getAllEthTransfers.transfers
                            const prevT = previousResult.getAllEthTransfers.transfers
                            return {
                                ...previousResult,
                                getAllEthTransfers: {
                                    __typename: previousResult.getAllEthTransfers.__typename,
                                    nextKey: fetchMoreResult.getAllEthTransfers.nextKey,
                                    transfers: [...prevT, ...newT]
                                }
                            }
                        }
                    })
                }
            }

            this.index = page
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
     * Emit error to Sentry
     * @param val {Boolean}
     */
    emitErrorState(val: boolean): void {
        this.hasError = val
        this.$emit('errorTxs', this.hasError, ErrorMessageBlock.blockTxs)
    }
    /*
    ===================================================================================
      Watch:
    ===================================================================================
    */
    @Watch('blockRef')
    onBlockRefChanged(newVal: string, oldVal: string) {
        if (newVal !== oldVal) {
            this.initialLoad = true
            this.hasError = false
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
