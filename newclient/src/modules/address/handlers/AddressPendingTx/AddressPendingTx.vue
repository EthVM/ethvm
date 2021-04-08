<template>
    <v-card color="white" flat class="pb-2">
        <app-table-title :has-pagination="showPagination" :title="getTitle" :page-type="pageType" page-link="">
            <template v-if="showPagination" #pagination>
                <app-paginate :class="$vuetify.breakpoint.smAndDown ? 'pt-3' : ''" :total="totalPages" :current-page="index" @newPage="setPage" />
            </template>
        </app-table-title>
        <table-txs :is-loading="loading" :table-message="message" :txs-data="pendingTx" :is-scroll-view="false">
            <template #header>
                <table-address-txs-header :address="address" :is-pending="true" />
            </template>
            <template #rows>
                <v-card v-for="tx in pendingTx" :key="tx.getHash()" class="transparent" flat>
                    <table-address-txs-row v-if="tx" :is-pending="true" :transfer="tx" :address="address" :is-mined-indicator="minedIndicator[tx.getHash()]" />
                </v-card>
            </template>
        </table-txs>
    </v-card>
</template>

<script lang="ts">
import AppPaginate from '@app/core/components/ui/AppPaginate.vue'
import AppTableTitle from '@app/core/components/ui/AppTableTitle.vue'
import TableTxs from '@app/modules/txs/components/TableTxs.vue'
import TableAddressTxsHeader from '@app/modules/address/components/TableAddressTxsHeader.vue'
import TableAddressTxsRow from '@app/modules/address/components/TableAddressTxsRow.vue'
import TableAddressTokensHeader from '@app/modules/address/components/TableAddressTokensHeader.vue'
import TableAddressTransfersRow from '@app/modules/address/components/TableAddressTransfersRow.vue'
import { Component, Prop, Watch, Mixins } from 'vue-property-decorator'
import BN from 'bignumber.js'
import { getPendingTransactions, pendingTransaction } from './pendingTxs.graphql'
import { getPendingTransactions_getPendingTransactions as PendingTxType } from './apolloTypes/getPendingTransactions'
import { pendingTransaction_pendingTransaction as PendingTransferType } from './apolloTypes/pendingTransaction'
import { EthTransfer } from '@app/modules/address/models/EthTransfer'
import { throwError } from 'rxjs'
import { networkInterfaces } from 'os'
import { ErrorMessage } from '@app/modules/address/models/ErrorMessagesForAddress'
import { onError } from 'apollo-link-error'
import { NewBlockSubscription } from '@app/modules/blocks/NewBlockSubscription/newBlockSubscription.mixin'
import { getBlockTransfers } from '@app/modules/txs/handlers/BlockTxs/queryTransfers.graphql'

interface PendingMap {
    [key: string]: EthTransfer
}
interface MinedMap {
    [key: string]: boolean
}

@Component({
    components: {
        AppTableTitle,
        TableTxs,
        TableAddressTxsRow,
        TableAddressTxsHeader,
        TableAddressTokensHeader,
        TableAddressTransfersRow,
        AppPaginate
    },
    apollo: {
        getPendingTx: {
            query: getPendingTransactions,
            variables() {
                return {
                    hash: this.address
                }
            },
            deep: true,
            update: data => {
                return data.getPendingTransactions
            },
            result({ data }) {
                if (data && data.getPendingTransactions) {
                    this.emitErrorState(false)
                    if (this.initialLoad) {
                        try {
                            this.pendingSorted = [...this.getPendingTx.sort((x, y) => (y.timestamp < x.timestamp ? -1 : y.timestamp > x.timestamp ? 1 : 0))]
                            this.getPendingTx.forEach(i => {
                                this.pendingMap[i.hash.toLowerCase()] = new EthTransfer(i)
                            })
                            this.initialLoad = false
                        } catch (error) {
                            this.emitErrorState(true)
                            throw error
                        }
                    }
                } else {
                    this.emitErrorState(true)
                }
            },
            error(error) {
                this.emitErrorState(true)
            }
        },
        getBlockTransfers: {
            query: getBlockTransfers,
            fetchPolicy: 'network-only',
            skip() {
                return this.newBlockNumber === undefined || this.pendingSorted.length < 1
            },
            variables() {
                return { _number: this.newBlockNumber }
            },
            result({ data }) {
                if (data && data.getBlockTransfers && data.getBlockTransfers.transfers && data.getBlockTransfers.transfers.length > 0) {
                    data.getBlockTransfers.transfers.forEach(i => {
                        if (i.transfer.transactionHash && this.pendingMap[i.transfer.transactionHash]) {
                            this.markMined(i.transfer.transactionHash.toLowerCase())
                        }
                    })
                }
            },
            error(error) {
                this.emitErrorState(true)
            }
        },
        $subscribe: {
            newPendingTransfer: {
                query: pendingTransaction,
                variables() {
                    return {
                        owner: this.address
                    }
                },
                skip() {
                    return this.loading
                },
                result({ data }) {
                    if (data && data.pendingTransaction) {
                        try {
                            const newTx = data.pendingTransaction
                            this.pendingMap[newTx.transactionHash.toLowerCase()] = new EthTransfer(newTx)
                            this.insertItem(newTx)
                        } catch (error) {
                            this.emitErrorState(true)
                            throw error
                        }
                    } else {
                        this.emitErrorState(true)
                    }
                },
                error(error) {
                    this.emitErrorState(true)
                }
            }
        }
    }
})
export default class AddressPendingTx extends Mixins(NewBlockSubscription) {
    /*
    ===================================================================================
      Props
    ===================================================================================
    */
    @Prop(Number) maxItems!: number
    @Prop(String) address!: string

    /*
  ===================================================================================
    Initial Data
  ===================================================================================
  */
    index = 0
    pageType = 'address'
    getPendingTx!: PendingTxType[]
    pendingSorted: (PendingTransferType | PendingTxType)[] = []
    pendingMap: PendingMap = {}
    initialLoad = true
    hasError = false
    minedIndicator: MinedMap = {}

    /*
    ===================================================================================
      Computed
    ===================================================================================
    */
    get pendingTx(): EthTransfer[] {
        if (!this.loading && this.pendingSorted) {
            try {
                const start = this.index * this.maxItems
                const end = start + this.maxItems > this.pendingSorted.length ? this.pendingSorted.length : start + this.maxItems
                return this.pendingSorted
                    .slice(start, end)
                    .map(i => {
                        const hash = i.__typename === 'Tx' ? i.hash.toLowerCase() : i.transactionHash.toLowerCase()
                        return this.pendingMap[hash]
                    })
                    .filter(i => {
                        return i ? true : false
                    })
            } catch (error) {
                this.emitErrorState(true)
                throw error
            }
        }
        return []
    }

    get getTitle(): string {
        return `${this.$tc('tx.pending', 2)}`
    }

    get loading(): boolean {
        return this.$apollo.queries.getPendingTx.loading || this.initialLoad
    }

    get message(): string {
        if (!this.loading && this.pendingTx.length === 0) {
            return `${this.$t('message.tx.no-pending')}`
        }
        return ''
    }

    get totalPages(): number {
        if (this.pendingSorted) {
            return Math.ceil(new BN(this.pendingSorted.length).div(this.maxItems).toNumber())
        }
        return 0
    }

    get hasMore(): boolean {
        return this.index + 1 < this.totalPages
    }

    get showPagination(): boolean {
        return this.totalPages > 1
    }

    /*
    ===================================================================================
      Methods:
    ===================================================================================
    */
    /**
     * Sets page number
     * @param page {Number}
     */
    setPage(page: number): void {
        if (page < this.index || this.hasMore) {
            this.index = page
        }
    }

    /*
    Desc: This method inserts new transfer into an array in sorted manner.
    Assumptions:
        - Array is already presorted.
        - Most of the new pending transfers will come as soon as they are seen and processed, according to the timestamp.
        - Case I: if first item in the array, has smaller timestamp item will be inserted in front of the array
        - Case II: Items can come in unsorted, as this depends on how busy the account is; in this case binary index search will be performed and item will be inserted accordignly
     */
    insertItem(item: PendingTransferType): void {
        const itemTime = item.timestamp
        if (this.pendingSorted.length === 0) {
            this.$set(this.pendingSorted, 0, item)
        } else if (this.pendingSorted[0].timestamp !== null && this.pendingSorted[0].timestamp <= itemTime) {
            this.pendingSorted.splice(0, 0, item)
        } else {
            let searchIndex = true
            let start = 0
            let end = this.pendingSorted.length
            while (searchIndex) {
                const mIndex = new BN(end + start).idiv(2).toNumber()
                const midTime = this.pendingSorted[mIndex].timestamp
                if (midTime === null) {
                    end === this.pendingSorted.length
                    searchIndex = false
                } else if (end - start === 1) {
                    searchIndex = false
                } else {
                    if (midTime === itemTime) {
                        end = mIndex
                        searchIndex = false
                    } else if (midTime < itemTime) {
                        end = mIndex
                    } else {
                        start = mIndex
                    }
                }
            }
            this.pendingSorted.splice(end, 0, item)
        }
    }
    /*
    Desc:
      - Marks a transfer as mined
      - After 10 seconds:
          - it finds transfer index
          - deletes transfer
          - checks whether or not to edit pagination indexes

     */

    markMined(hash: string): void {
        this.$set(this.minedIndicator, hash, true)
        setTimeout(() => {
            const index = this.pendingSorted
                .map(item => {
                    return item.__typename === 'Tx' ? item.hash : item.transactionHash
                })
                .indexOf(hash)
            if (index >= 0) {
                this.pendingSorted.splice(index, 1)
                if (this.totalPages < this.index + 1) {
                    this.index = this.totalPages - 1
                }
            }
            /* Check Pagination View: */
        }, 10000)
    }

    emitErrorState(val: boolean): void {
        this.hasError = val
        this.$emit('errorPending', this.hasError, ErrorMessage.pending)
    }
}
</script>
