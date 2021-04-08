<template>
    <v-card color="white" flat class="pb-2">
        <!--
        =====================================================================================
          Title / Total
        =====================================================================================
        -->
        <v-layout align-center justify-start row wrap pl-3 pr-3 mt-1>
            <app-table-title :title="title" :has-pagination="false" :page-type="pageType" :title-caption="totalText" page-link="" class="pl-2" />
            <v-spacer hidden-xs-only />
        </v-layout>
        <v-divider class="lineGrey mt-1 mb-1" />
        <!--
        =====================================================================================
          Search / Pagination / Filter
        =====================================================================================
        -->
        <v-layout align-center justify-center row wrap px-2 my-2 class="search-paginate-filter-layout">
            <v-flex xs12 md7 py-0 mb-1>
                <fav-search :items="favTokens" :loading="isLoading" @search="onSearch" />
            </v-flex>
            <v-spacer hidden-sm-and-up />
            <v-flex xs12 sm5 py-0 hidden-md-and-up>
                <app-filter :options="options" :show-desktop="false" :is-sort="true" @onSelectChange="sortTokens" />
            </v-flex>
            <v-spacer hidden-md-and-up />
            <v-flex shrink py-0>
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
            :txs-data="tokenList"
            :is-scroll-view="false"
            :table-message="message"
        >
            <template #header>
                <table-tokens-header :loading="isLoading" :sort="sort" @sortBy="sortTokens" />
            </template>
            <template #rows>
                <v-card v-for="(token, index) in tokenList" :key="index" class="transparent" flat>
                    <table-tokens-row :token="token" />
                </v-card>
            </template>
        </table-txs>
    </v-card>
</template>

<script lang="ts">
import { Component, Prop, Mixins, Watch } from 'vue-property-decorator'
import { ErrorMessagesFav } from '@app/modules/favorite-tokens/models/ErrorMessagesFav'
import { Crumb } from '@app/core/components/props'
import AppTableTitle from '@app/core/components/ui/AppTableTitle.vue'
import AppPaginate from '@app/core/components/ui/AppPaginate.vue'
import TableTokensHeader from '@app/modules/tokens/components/TableTokensHeader.vue'
import TableTokensRow from '@app/modules/tokens/components/TableTokensRow.vue'
import FavSearch from '@app/modules/favorite-tokens/components/FavSearch.vue'
import TableTxs from '@app/modules/txs/components/TableTxs.vue'
import { FavActions as FavActionsMixin } from '@app/modules/favorite-tokens/mixins/FavActions.mixin'
import { CoinData } from '@app/core/components/mixins/CoinData/CoinData.mixin'
import { favTokenCache } from '@app/apollo/favorite-tokens/rootQuery.graphql'
import { favTokenCache_favTokens as favTokensType } from '@app/apollo/favorite-tokens/apolloTypes/favTokenCache'
import { FILTER_VALUES, FavSort } from '@app/modules/favorite-tokens/models/FavSort'
import { getLatestPrices_getLatestPrices as TokenMarketData } from '@app/core/components/mixins/CoinData/apolloTypes/getLatestPrices.ts'
import AppFilter from '@app/core/components/ui/AppFilter.vue'

import BN from 'bignumber.js'

@Component({
    components: {
        AppTableTitle,
        AppPaginate,
        FavSearch,
        TableTxs,
        TableTokensHeader,
        TableTokensRow,
        AppFilter
    },
    apollo: {
        favTokens: {
            query: favTokenCache,
            client: 'FavTokClient',
            fetchPolicy: 'network-only',
            update: data => data.favTokens,
            result({ data }) {
                if (data && data.favTokens) {
                    this.emitErrorState(false)
                }
            },
            error(error) {
                this.emitErrorState(true, ErrorMessagesFav.addressCheck)
            }
        }
    }
})
export default class FavHandlerTokensListRow extends Mixins(CoinData, FavActionsMixin) {
    /*
    ===================================================================================
      Data:
    ===================================================================================
    */
    pageType = 'fav_tokens'
    index = 0
    favTokens!: favTokensType[]
    hasError = false
    maxItems = 10
    /* Search */
    searchVal = ''
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
                text: this.$i18n.tc('token.name', 1),
                filter: this.$i18n.t('filter.high')
            },
            {
                value: FILTER_VALUES[1],
                text: this.$i18n.tc('token.name', 1),
                filter: this.$i18n.t('filter.low')
            },
            {
                value: FILTER_VALUES[2],
                text: this.$i18n.tc('price.name', 1),
                filter: this.$i18n.t('filter.high')
            },
            {
                value: FILTER_VALUES[3],
                text: this.$i18n.tc('price.name', 1),
                filter: this.$i18n.t('filter.low')
            },
            {
                value: FILTER_VALUES[4],
                text: this.$i18n.tc('token.volume', 1),
                filter: this.$i18n.t('filter.high')
            },
            {
                value: FILTER_VALUES[5],
                text: this.$i18n.tc('token.volume', 1),
                filter: this.$i18n.t('filter.low')
            },
            {
                value: FILTER_VALUES[6],
                text: this.$i18n.t('token.market'),
                filter: this.$i18n.t('filter.high')
            },
            {
                value: FILTER_VALUES[7],
                text: this.$i18n.t('token.market'),
                filter: this.$i18n.t('filter.low')
            }
        ]
    }

    get title(): string {
        return `${this.$t('token.favorite-title')}`
    }
    get totalFavorites(): number | null {
        return this.favorites ? this.favorites.length : null
    }

    get totalText(): string {
        return `${this.$t('contract.total')}: ${this.totalFavorites}`
    }

    get isLoading(): boolean {
        return this.isLoadingTokensMarketData
    }
    get hasFavTokens(): boolean {
        return this.totalPages > 0
    }
    get coinMarketFavorites(): TokenMarketData[] {
        if (!this.isLoading && this.hasFavTokens) {
            const coinMarketParams = this.favorites.map(item => {
                return item.address
            })
            const coinMarketInfo = this.getEthereumTokensMap(coinMarketParams)
            const _favorites: TokenMarketData[] = []
            this.favorites.forEach(token => {
                const getTokenFromCoinMarketList = coinMarketInfo ? coinMarketInfo.get(token.address) : undefined
                if (getTokenFromCoinMarketList) {
                    _favorites.push(Object.assign({}, getTokenFromCoinMarketList))
                } else {
                    _favorites.push({
                        contract: token.address,
                        current_price: 0,
                        id: token.symbol,
                        image: '',
                        market_cap: 0,
                        name: token.symbol.charAt(0).toUpperCase() + token.symbol.slice(1),
                        price_change_percentage_24h: 0,
                        symbol: token.symbol,
                        total_supply: '0',
                        total_volume: 0,
                        __typename: 'TokenMarketInfo'
                    })
                }
            })

            return _favorites
        }
        return []
    }
    get tokenList(): TokenMarketData[] {
        if (!this.isLoading || this.hasFavTokens) {
            this.sort !== '' ? this.favSort.sortFavorites(this.coinMarketFavorites, this.sort) : ''
            const start = this.searchVal ? 0 : this.index * this.maxItems
            const end = start + this.maxItems > this.coinMarketFavorites.length ? this.coinMarketFavorites.length : start + this.maxItems
            return this.coinMarketFavorites.slice(start, end)
        }
        return []
    }
    get totalPages(): number {
        if (this.favorites && this.favorites.length) {
            return Math.ceil(new BN(this.favorites.length).div(this.maxItems).toNumber())
        }
        return 0
    }
    get favorites(): favTokensType[] {
        const searchResults = this.searchVal
            ? this.favTokens.filter(item => item.symbol.toLowerCase().includes(this.searchVal) || item.address.toLowerCase().includes(this.searchVal))
            : []
        return searchResults && this.searchVal ? searchResults : this.favTokens
    }
    get message(): string {
        if (!this.isLoading) {
            if (this.searchVal && (!this.totalFavorites || this.totalFavorites < 1)) {
                return this.$t('fav.message.no-search-results').toString()
            }
            if (!this.totalFavorites || this.totalFavorites < 1) {
                return this.$t('fav.message.no-tokens').toString()
            }
        }
        return ''
    }

    get favSort(): FavSort {
        return new FavSort(this.favorites)
    }

    /*
  ===================================================================================
    Methods
  ===================================================================================
  */

    sortTokens(sort: string): void {
        this.sort = sort
    }
    emitErrorState(val: boolean, message: string): void {
        this.hasError = val
        this.$emit('errorFavorites', this.hasError, message)
    }

    onSearch(val): void {
        this.searchVal = val ? val.toLowerCase() : null
    }
    setPage(page: number, reset: boolean = false): void {
        if (reset) {
            this.index = 0
        } else {
            this.index = page
        }
    }
}
</script>

<style scoped lang="css">
.v-btn {
    min-width: 40px !important;
}
.search-paginate-filter-layout {
    min-height: 52px;
}
</style>
