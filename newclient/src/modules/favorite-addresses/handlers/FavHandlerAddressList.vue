<template>
    <v-card color="white" flat class="pb-2">
        <!--
        =====================================================================================
          Desktop (md-and-up): TITLE / ADD button / CANCEL button / REMOVE button
          Mobile (SM): TITLE / ADD button / CHECK ALL button / CANCEL button/ REMOVE button
          Mobile (xs-only): TITLE
        =====================================================================================
        -->
        <v-layout align-center justify-start row wrap pl-3 pr-3 mt-1>
            <app-table-title :title="title" :has-pagination="false" :page-type="pageType" :title-caption="titleCaption" page-link="" class="pl-2" />
            <v-flex v-if="!deleteMode" shrink pl-2 hidden-xs-only>
                <fav-btn-add ref="favAdd" :add-address="addItem" :has-address="hasAddress" />
            </v-flex>
            <v-spacer hidden-xs-only />
            <v-flex v-if="deleteMode" shrink hidden-xs-only hidden-md-and-up>
                <v-layout row align-center justify-start>
                    <app-check-box :values-array="deleteArray" :is-select-all="true" :all-selected="isAllSelected" @selectAll="removeAll" />
                    <p class="caption black--text">{{ $t('fav.select-all') }}</p>
                </v-layout>
            </v-flex>
            <v-flex v-if="deleteMode" hidden-xs-only shrink>
                <v-btn flat small color="error" class="text-capitalize ma-0" @click="closeDeleteMode()"> {{ $t('common.cancel') }}</v-btn>
            </v-flex>
            <v-flex shrink pl-2 hidden-xs-only>
                <fav-btn-remove
                    :start-delete-mode="changeDeleteMode"
                    :delete-mode="deleteMode"
                    :delete-array="dialogDeleteArray"
                    :delete-method="deleteAddrs"
                />
            </v-flex>
        </v-layout>
        <v-divider class="lineGrey mt-1 mb-1" />
        <!--
        =====================================================================================
        SM AND UP: Search / Pagination
        =====================================================================================
        -->
        <v-layout align-center justify-center row wrap hidden-xs-only px-2 my-2>
            <v-flex sm12 md7>
                <fav-search :items="favAddresses" :loading="isLoading" @search="onSearch" />
            </v-flex>
            <v-flex sm5 py-0 hidden-md-and-up>
                <app-filter :options="options" :show-desktop="false" :is-sort="true" @onSelectChange="sortAddresses" />
            </v-flex>
            <v-spacer />
            <v-flex shrink py-0 pl-1>
                <app-paginate v-if="totalPages > 1" :total="totalPages" :current-page="index" @newPage="setPage" />
            </v-flex>
        </v-layout>

        <!--
        =====================================================================================
          Mobile (xs-only): ADD button / REMOVE button
        =====================================================================================
        -->
        <v-layout hidden-sm-and-up align-center justify-space-between pl-2 pr-2 pb-2>
            <v-flex v-if="!deleteMode" shrink pt-0 pb-0>
                <fav-btn-add ref="favAdd" :add-address="addItem" :has-address="hasAddress" />
            </v-flex>
            <v-flex v-if="deleteMode" shrink pb-0>
                <v-layout row align-center justify-start>
                    <app-check-box :values-array="deleteArray" :is-select-all="true" :all-selected="isAllSelected" @selectAll="removeAll" />
                    <p class="caption black--text">{{ $t('fav.select-all') }}</p>
                </v-layout>
            </v-flex>
            <v-flex v-if="deleteMode" shrink pb-0>
                <v-btn v-if="deleteMode" flat small color="error" class="text-capitalize ma-0" @click="closeDeleteMode()"> {{ $t('common.cancel') }}</v-btn>
            </v-flex>
            <v-flex shrink pb-0 pt-1>
                <fav-btn-remove
                    :start-delete-mode="changeDeleteMode"
                    :delete-mode="deleteMode"
                    :delete-array="dialogDeleteArray"
                    :delete-method="deleteAddrs"
                />
            </v-flex>
        </v-layout>
        <!--
        =====================================================================================
          Mobile (xs-only): Search / Pagination
        =====================================================================================
        -->
        <v-layout grid-list-xs align-center justify-center hidden-sm-and-up row wrap pr-2 pl-2 mb-3>
            <v-flex xs12 pb-0 pt-0>
                <app-filter :options="options" :show-desktop="false" :is-sort="true" @onSelectChange="sortAddresses" />
            </v-flex>
            <v-flex xs12 pb-0 pt-0>
                <fav-search :items="favAddresses" :loading="isLoading" @search="onSearch" />
            </v-flex>
            <v-flex shrink pt-0 pb-0>
                <app-paginate v-if="totalPages > 1" :total="totalPages" :current-page="index" @newPage="setPage" />
            </v-flex>
        </v-layout>
        <!--
        =====================================================================================
          Fav Address Table
        =====================================================================================
        -->
        <table-txs
            :max-items="maxItems"
            :index="index"
            :is-loading="isLoading || hasError"
            :txs-data="adrList"
            :is-scroll-view="false"
            :table-message="message"
        >
            <template #header>
                <fav-addr-table-header
                    :loading="isLoading"
                    :delete-mode="deleteMode"
                    :all-selected="isAllSelected"
                    :delete-array="deleteArray"
                    @sortBy="sortAddresses"
                    @selectAllInHeader="removeAll"
                />
            </template>
            <template #rows>
                <v-card v-for="adr in adrList" :key="adr.hash" class="transparent" flat>
                    <fav-addr-table-row-handler
                        :ether-price="ethPrice"
                        :hash="adr.address"
                        :name="adr.name"
                        :delete-mode="deleteMode"
                        :delete-array="deleteArray"
                        :check-box-method="updateDeleteArray"
                        @errorFavorites="emitErrorState"
                        @addressChips="updateChips"
                    />
                </v-card>
            </template>
        </table-txs>
    </v-card>
</template>

<script lang="ts">
import { Component, Prop, Mixins, Watch } from 'vue-property-decorator'
import { ErrorMessagesFav } from '@app/modules/favorite-addresses/models/ErrorMessagesFav'
import { Crumb } from '@app/core/components/props'
import AppTableTitle from '@app/core/components/ui/AppTableTitle.vue'
import AppPaginate from '@app/core/components/ui/AppPaginate.vue'
import FavAddrTableHeader from '@app/modules/favorite-addresses/components/FavAddrTableHeader.vue'
import FavAddrTableRowHandler from '@app/modules/favorite-addresses/handlers/FavHandlerAddressListRow.vue'
import FavBtnAdd from '@app/modules/favorite-addresses/components/FavBtnAdd.vue'
import FavBtnRemove from '@app/modules/favorite-addresses/components/FavBtnRemove.vue'
import FavSearch from '@app/modules/favorite-addresses/components/FavSearch.vue'
import TableTxs from '@app/modules/txs/components/TableTxs.vue'
import { FavActions as FavActionsMixin } from '@app/modules/favorite-addresses/mixins/FavActions.mixin'
import { CoinData } from '@app/core/components/mixins/CoinData/CoinData.mixin'
import { favAddressCache } from '@app/apollo/favorite-addresses/rootQuery.graphql'
import { favAddressCache_favAddresses as favAddressesType } from '@app/apollo/favorite-addresses/apolloTypes/favAddressCache'
import AppCheckBox from '@app/core/components/ui/AppCheckBox.vue'
import { DialogAddress } from '@app/modules/favorite-addresses/models/FavDialog'
import { EnumAdrChips } from '@app/core/components/props'
import { FILTER_VALUES, FavSort } from '@app/modules/favorite-addresses/models/FavSort'
import AppFilter from '@app/core/components/ui/AppFilter.vue'
import { DataArray } from '@app/apollo/favorite-addresses/models'

import BN from 'bignumber.js'

@Component({
    components: {
        AppCheckBox,
        AppTableTitle,
        AppPaginate,
        FavBtnAdd,
        FavBtnRemove,
        FavAddrTableHeader,
        FavAddrTableRowHandler,
        FavSearch,
        TableTxs,
        AppFilter
    },
    apollo: {
        favAddresses: {
            query: favAddressCache,
            client: 'FavAddrClient',
            fetchPolicy: 'network-only',
            update: data => data.favAddresses,
            result({ data }) {
                if (data && data.favAddresses) {
                    this.emitErrorState(false)
                }
            },
            error(error) {
                this.emitErrorState(true, ErrorMessagesFav.addressCheck)
            }
        }
    }
})
export default class FavHandlerAddressListRow extends Mixins(CoinData, FavActionsMixin) {
    /*
    ===================================================================================
      Data:
    ===================================================================================
    */
    pageType = 'fav_addresses'
    index = 0
    favAddresses!: favAddressesType[]
    hasError = false
    maxItems = 10
    /* Search */
    searchVal = ''
    /* Delete Mode */
    deleteMode = false
    deleteArray: string[] = []
    isAllSelected = false
    addressChipsMap = new Map<string, EnumAdrChips[]>()
    sort = ''

    /*
  ===================================================================================
    Computed
  ===================================================================================
  */
    get options() {
        return [
            {
                value: FILTER_VALUES[0],
                text: this.$i18n.tc('fav.sort.address'),
                filter: this.$i18n.t('filter.high')
            },
            {
                value: FILTER_VALUES[1],
                text: this.$i18n.tc('fav.sort.address'),
                filter: this.$i18n.t('filter.low')
            },
            {
                value: FILTER_VALUES[2],
                text: this.$i18n.tc('fav.sort.name'),
                filter: this.$i18n.t('filter.high')
            },
            {
                value: FILTER_VALUES[3],
                text: this.$i18n.tc('fav.sort.name'),
                filter: this.$i18n.t('filter.low')
            }
        ]
    }

    get title(): string {
        return this.$t('fav.title').toString()
    }
    get totalAddr(): number | null {
        return this.favorites ? this.favorites.length : null
    }
    get isLoading(): boolean {
        return this.$apollo.queries.favAddresses.loading
    }
    get hasFavAdr(): boolean {
        return this.totalPages > 0
    }
    get adrList(): favAddressesType[] {
        if (!this.isLoading || this.hasFavAdr) {
            this.sort !== '' ? this.favSort.sortFavorites(this.favorites, this.sort) : ''
            const start = this.searchVal ? 0 : this.index * this.maxItems
            const end = start + this.maxItems > this.favorites.length ? this.favorites.length : start + this.maxItems
            return this.favorites.slice(start, end)
        }
        return []
    }
    get totalPages(): number {
        if (this.favorites && this.favorites.length) {
            return Math.ceil(new BN(this.favorites.length).div(this.maxItems).toNumber())
        }
        return 0
    }
    get favorites(): favAddressesType[] {
        const searchResults = this.searchVal
            ? this.favAddresses.filter(item => item.name.toLowerCase().includes(this.searchVal) || item.address.toLowerCase().includes(this.searchVal))
            : []
        return searchResults && this.searchVal ? searchResults : this.favAddresses
    }
    get message(): string {
        if (!this.isLoading) {
            if (this.searchVal && (!this.totalAddr || this.totalAddr < 1)) {
                return this.$t('fav.message.no-search-results').toString()
            }
            if (!this.totalAddr || this.totalAddr < 1) {
                return this.$t('fav.message.no-addr').toString()
            }
        }
        return ''
    }

    get titleCaption(): string {
        return `${this.$t('contract.total')}: ${this.totalAddr}`
    }

    get dialogDeleteArray(): DialogAddress[] {
        const addrDelete = this.deleteArray.map(_hash => {
            const addr = new DialogAddress(_hash)
            const hasName = this.favAddresses.find(i => i.address === _hash)
            if (hasName) {
                addr.setName(hasName.name)
            }
            const hasChips = this.addressChipsMap.get(_hash)
            if (hasChips) {
                addr.setChips(hasChips)
            }
            return addr
        })
        return addrDelete
    }
    get favSort(): FavSort {
        return new FavSort(this.favorites)
    }

    /*
  ===================================================================================
    Methods
  ===================================================================================
  */
    hasAddress(address: string): boolean {
        const foundItems = this.favAddresses.find(i => i.address === address)
        return foundItems ? true : false
    }
    sortAddresses(sort: string): void {
        this.sort = sort
    }
    emitErrorState(val: boolean, message: string): void {
        this.hasError = val
        this.$emit('errorFavorites', this.hasError, message)
    }

    onSearch(val): void {
        this.searchVal = val ? val.toLowerCase() : null
    }
    addItem(item): void {
        this.mixinAddToFav(item.name, item.address)
    }
    changeDeleteMode(): void {
        this.deleteMode = !this.deleteMode
    }
    closeDeleteMode(): void {
        this.deleteMode = false
        this.deleteArray = []
    }
    setPage(page: number, reset: boolean = false): void {
        if (reset) {
            this.index = 0
        } else {
            this.index = page
        }
    }
    updateDeleteArray(newArray: string[]): void {
        this.deleteArray = newArray
        if (this.deleteArray.length === this.totalAddr) {
            this.isAllSelected = true
        }
        if (this.deleteArray.length !== this.totalAddr) {
            this.isAllSelected = false
        }
    }
    removeAll(): void {
        this.isAllSelected = !this.isAllSelected
        if (this.isAllSelected) {
            this.deleteArray = this.favAddresses.map(i => i.address)
        } else {
            this.deleteArray = []
        }
    }
    deleteAddrs(): void {
        this.deleteArray.forEach(_hash => {
            this.mixinRemoveFromFav(_hash)
        })
        this.deleteMode = false
        this.deleteArray = []
    }
    updateChips(chips: EnumAdrChips[], hash: string): void {
        this.addressChipsMap.set(hash, chips)
    }
}
</script>

<style scoped lang="css">
.v-btn {
    min-width: 40px !important;
}
</style>
