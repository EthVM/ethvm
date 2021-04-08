<template>
    <v-card color="white" flat class="pt-3 pb-2">
        <app-table-title :title="$tc('tx.pending', 2)" :has-pagination="showPagination" page-type="pending" page-link="/pending-txs">
            <template v-if="showPagination && !initialLoad" #pagination>
                <app-paginate :total="totalPages" :current-page="index" @newPage="setPage" />
            </template>
        </app-table-title>
        <table-txs
            :max-items="maxItems"
            :index="index"
            :is-loading="loading"
            :table-message="message"
            :txs-data="pendingTxs"
            :is-scroll-view="false"
            :pending="true"
        />
        <v-layout
            v-if="showPagination && !initialLoad"
            :justify-end="$vuetify.breakpoint.mdAndUp"
            :justify-center="$vuetify.breakpoint.smAndDown"
            row
            class="pb-1 pr-3 pl-2"
        >
            <app-paginate :total="totalPages" :current-page="index" @newPage="setPage" />
        </v-layout>
    </v-card>
</template>

<script lang="ts">
import AppTableTitle from '@app/core/components/ui/AppTableTitle.vue'
import AppPaginate from '@app/core/components/ui/AppPaginate.vue'
import TableTxs from '@app/modules/txs/components/TableTxs.vue'
import { Component, Prop, Watch, Mixins } from 'vue-property-decorator'
import BN from 'bignumber.js'
import { pendingTx } from './pendingTxs.graphql'
import { PendingTx } from '@app/modules/txs/components/props'
import { ErrorMessagePendingTx } from '@app/modules/txs/models/ErrorMessagesForTx'
import { NewBlockSubscription } from '@app/modules/blocks/NewBlockSubscription/newBlockSubscription.mixin'
import { getBlockTransfers } from '@app/modules/txs/handlers/BlockTxs/queryTransfers.graphql'

@Component({
    components: {
        AppTableTitle,
        AppPaginate,
        TableTxs
    },
    apollo: {
        getBlockTransfers: {
            query: getBlockTransfers,
            fetchPolicy: 'network-only',
            skip() {
                return this.newBlockNumber === undefined && this.pendingTxs.length < 1
            },
            variables() {
                return { _number: this.newBlockNumber }
            },
            result({ data }) {
                if (data && data.getBlockTransfers) {
                    if (data.getBlockTransfers.transfers.length > 0 && this.pendingTxs.length > 0) {
                        data.getBlockTransfers.transfers.forEach(i => {
                            if (i.transfer.transactionHash) {
                                this.markMined(i.transfer.transactionHash)
                            }
                        })
                    }
                }
            },
            error(error) {
                this.emitErrorState(true)
            }
        },
        $subscribe: {
            NewPendingTx: {
                query: pendingTx,
                result({ data }) {
                    if (this.initialLoad) {
                        this.initialLoad = false
                    }
                    if (data.pendingTransaction) {
                        data.pendingTransaction.isMined = false
                        this.pendingTxs.push(data.pendingTransaction)
                    }
                },
                error(error) {
                    this.emitErrorState(true)
                }
            }
        }
    }
})
export default class PendingTxs extends Mixins(NewBlockSubscription) {
    /*
    ===================================================================================
      Props
    ===================================================================================
    */

    @Prop(Number) maxItems!: number
    /*
    ===================================================================================
     Initial Data
    ===================================================================================
    */
    pendingTxs: PendingTx[] = []
    initialLoad = true
    index = 0
    hasError = false

    /*
    ===================================================================================
      Computed
    ===================================================================================
    */
    get totalPages(): number {
        if (this.pendingTxs.length > 0) {
            return Math.ceil(new BN(this.pendingTxs.length).div(this.maxItems).toNumber())
        }
        return 0
    }

    get message(): string {
        if (this.loading) {
            return ''
        }
        if (!this.initialLoad && this.pendingTxs.length === 0) {
            return `${this.$t('message.tx.no-pen')}`
        }
        return ''
    }

    get showPagination(): boolean {
        return this.totalPages > 1
    }

    get loading(): boolean {
        if (this.hasError) {
            return true
        }
        return this.pendingTxs.length === 0
    }
    get hasMore(): boolean {
        return this.index + 1 < this.totalPages
    }

    /*
    ===================================================================================
      Methods:
    ===================================================================================
    */
    /**
     * Marks pending tx as mined
     * @param _hash {String}
     */
    markMined(hash: string): void {
        let index = this.pendingTxs.findIndex(tx => tx.transactionHash === hash)
        if (index >= 0 && this.pendingTxs[index]) {
            this.pendingTxs[index].isMined = true
            setTimeout(() => {
                index = this.pendingTxs.findIndex(tx => tx.transactionHash === hash)
                if (index >= 0) {
                    this.pendingTxs.splice(index, 1)
                    if (this.totalPages < this.index + 1) {
                        this.index = this.totalPages - 1
                    }
                }
            }, 10000)
        }
    }
    /**
     * Sets page number
     * @param page {Number}
     */
    setPage(page: number): void {
        if (page < this.index || this.hasMore) {
            this.index = page
        }
    }
    /**
     * Emit error to Sentry
     * @param val {Boolean}
     */
    emitErrorState(val: boolean): void {
        this.hasError = val
        this.$emit('errorDetails', this.hasError, ErrorMessagePendingTx.details)
    }
}
</script>
