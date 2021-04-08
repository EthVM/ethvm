<template>
    <v-card color="white" flat class="pb-2">
        <div v-if="!showUniqueNFT">
            <app-table-title :title="getTitle" :has-pagination="showPagination" :page-type="pageType" page-link="">
                <!-- Notice new update-->
                <template v-if="!loading" #update>
                    <app-new-update :text="updateText" :update-count="newTokens" :hide-count="true" @reload="setPage(0, true)" />
                </template>
                <template #filter>
                    <app-filter :is-selected="tokenOptions[1]" :options="tokenOptions" :show-desktop="false" :is-sort="true" @onSelectChange="sortTokens" />
                </template>
                <template v-if="showPagination && !loading" #pagination>
                    <app-paginate :total="totalPages" :current-page="index" @newPage="setPage" />
                </template>
            </app-table-title>
            <table-txs :max-items="maxItems" :index="index" :is-loading="loading" :table-message="message" :txs-data="tokens" :is-scroll-view="false">
                <template #header>
                    <table-address-tokens-header
                        :is-erc20="isERC20"
                        :is-transfers="false"
                        :loading="loading"
                        :has-tokens="hasTokens && getTokens.length > 0"
                        @sortBy="sortTokens"
                    />
                </template>
                <template #rows>
                    <v-card v-for="(token, index) in tokens" :key="index" class="transparent" flat>
                        <table-address-tokens-row
                            v-if="!loading && tokenSort"
                            :token="token"
                            :holder="address"
                            :token-sort="tokenSort"
                            @showNft="showNftTokens"
                        />
                        <table-address-tokens-row-loading v-else />
                    </v-card>
                </template>
            </table-txs>
            <v-layout
                v-if="showPagination && !loading"
                :justify-end="$vuetify.breakpoint.mdAndUp"
                :justify-center="$vuetify.breakpoint.smAndDown"
                row
                class="pb-1 pr-3 pl-2"
            >
                <app-paginate :total="totalPages" :current-page="index" @newPage="setPage" />
            </v-layout>
        </div>
        <table-address-unique-nft
            v-else
            :contract-name="requestContractName"
            :contract="requestContract"
            :tokens="uniqueNFT"
            :loading="loadingUniqueNFT"
            @hideNFT="hideNFTTokens"
        />
    </v-card>
</template>

<script lang="ts">
import AppTableTitle from '@app/core/components/ui/AppTableTitle.vue'
import AppPaginate from '@app/core/components/ui/AppPaginate.vue'
import AppNewUpdate from '@app/core/components/ui/AppNewUpdate.vue'
import TableTxs from '@app/modules/txs/components/TableTxs.vue'
import AppFilter from '@app/core/components/ui/AppFilter.vue'
import TableAddressTokensRow from '@app/modules/address/components/TableAddressTokensRow.vue'
import TableAddressTokensRowLoading from '@app/modules/address/components/TableAddressTokensRow.vue'
import TableAddressTokensHeader from '@app/modules/address/components/TableAddressTokensHeader.vue'
import TableAddressUniqueNft from '@app/modules/address/components/TableAddressUniqueNft.vue'
import { Component, Prop, Watch, Mixins } from 'vue-property-decorator'
import BN from 'bignumber.js'
import { getOwnersERC20Tokens, getOwnersERC721Tokens, getOwnersERC721Balances, getNFTcontractsMeta } from './tokens.graphql'
import { getOwnersERC20Tokens_getOwnersERC20Tokens_owners as ERC20TokensType } from './apolloTypes/getOwnersERC20Tokens'
import { getOwnersERC721Balances_getOwnersERC721Balances as ERC721BalanceType } from './apolloTypes/getOwnersERC721Balances'
import {
    getOwnersERC721Tokens_getOwnersERC721Tokens as ERC721ContractTokensType,
    getOwnersERC721Tokens_getOwnersERC721Tokens_tokens as ERC721TokenType
} from './apolloTypes/getOwnersERC721Tokens'
import {
    getNFTcontractsMeta_getNFTcontractsMeta_tokenContracts_primary_asset_contracts as NFTMetaType,
    getNFTcontractsMeta_getNFTcontractsMeta as getNFTcontractsMetaType
} from './apolloTypes/getNFTcontractsMeta'
import { getLatestPrices_getLatestPrices as TokenMarketData } from '@app/core/components/mixins/CoinData/apolloTypes/getLatestPrices'
import { CoinData } from '@app/core/components/mixins/CoinData/CoinData.mixin'
import { AddressEventType } from '@app/apollo/global/globalTypes'
import { ErrorMessage } from '../../models/ErrorMessagesForAddress'
import { excpInvariantViolation } from '@app/apollo/exceptions/errorExceptions'
import { TOKEN_FILTER_VALUES, TokenSort, Token, NFTMetaMap } from '@app/modules/address/models/TokenSort'

/*
  DEV NOTES:
  - add on Error
  - add messages if Error to be displayed in Table
*/
interface NFTMap {
    [key: string]: ERC721TokenType[]
}

@Component({
    components: {
        AppNewUpdate,
        AppTableTitle,
        AppPaginate,
        TableTxs,
        TableAddressTokensHeader,
        TableAddressTokensRow,
        TableAddressTokensRowLoading,
        TableAddressUniqueNft,
        AppFilter
    },
    apollo: {
        getTokens: {
            query() {
                return this.isERC20 ? getOwnersERC20Tokens : getOwnersERC721Balances
            },
            fetchPolicy: 'network-only',
            variables() {
                return {
                    hash: this.address
                }
            },
            deep: true,
            update(data) {
                if (this.isERC20) {
                    return data.getOwnersERC20Tokens ? data.getOwnersERC20Tokens.owners : null
                }
                return data.getOwnersERC721Balances
            },
            result({ data }) {
                if (this.hasTokens) {
                    this.emitErrorState(false)
                    if (this.isERC20) {
                        this.hasNext = data.getOwnersERC20Tokens.nextKey || null
                        if (this.hasNext != null) {
                            this.fetchMore(this.hasNext)
                        } else {
                            this.totalTokens = this.getTokens.length
                            this.$emit('totalERC20', this.totalTokens)
                            this.$emit('loadingERC20Tokens', false)
                            this.initialLoad = false
                            if (this.totalTokens > this.maxItems) {
                                this.totalPages = Math.ceil(new BN(this.totalTokens).div(this.maxItems).toNumber())
                                this.showPagination = true
                            }
                        }
                    } else {
                        this.initialLoad = false
                    }
                } else {
                    this.emitErrorState(true)
                    this.showPagination = false
                    this.initialLoad = true
                }
            },
            error(error) {
                this.emitErrorState(true)
            }
        },
        getOwnersERC721Tokens: {
            query: getOwnersERC721Tokens,
            variables() {
                return {
                    hash: this.address,
                    tokenContract: this.requestContract
                }
            },
            skip() {
                return this.skipGetUniqueTokens
            },
            update: data => data.getOwnersERC721Tokens,
            result({ data }) {
                if (this.getOwnersERC721Tokens.tokens) {
                    if (this.getOwnersERC721Tokens.nextKey) {
                        this.fetchMoreUniqueNFT()
                    } else {
                        this.uniqueNFTMap[this.requestContract] = this.getOwnersERC721Tokens.tokens
                        this.uniqueNFT = this.uniqueNFTMap[this.requestContract]
                        this.loadingUniqueNFT = false
                        this.emitErrorState(false)
                        this.skipGetUniqueTokens = true
                    }
                } else {
                    this.emitErrorState(true)
                    throw new Error('getOwnersERC721Tokens.tokens returns null')
                }
            },
            error(error) {
                this.emitErrorState(true)
            }
        },
        getNFTcontractsMeta: {
            query: getNFTcontractsMeta,
            variables() {
                return {
                    address: this.address
                }
            },
            client: 'OpenSeaClient',
            update: data => data.getNFTcontractsMeta,
            result({ data }) {
                this.createNFTMetaMap()
            }
        }
    }
})
export default class AddressTokens extends Mixins(CoinData) {
    /*
    ===================================================================================
      Props
    ===================================================================================
    */

    @Prop(Number) maxItems!: number
    @Prop(String) address!: string
    @Prop({ type: String, default: 'eth' }) tokenType!: string
    @Prop(Number) newTokens!: number
    @Prop(Boolean) refetchTokens!: boolean

    /*
    ===================================================================================
     Initial Data
    ===================================================================================
    */

    syncing?: boolean = false
    initialLoad = true
    showPagination = false
    index = 0
    totalPages = 0
    /*isEnd -  Last Index loaded */
    isEnd = false
    pageType = 'address'
    getTokens!: ERC20TokensType[] | ERC721BalanceType[]
    hasNext!: string | null
    totalERC20 = 0
    totalERC721 = 0

    /* NFT Meta Info */
    getNFTcontractsMeta!: getNFTcontractsMetaType
    loadingMeta = true
    nftMeta: NFTMetaMap = {}

    /* Unique NFT List for contract */
    getOwnersERC721Tokens!: ERC721ContractTokensType
    uniqueNFTMap: NFTMap = {}
    loadingUniqueNFT = true
    showUniqueNFT = false
    uniqueNFT: ERC721TokenType[] = []
    requestContract = ''
    requestContractName = ''
    skipGetUniqueTokens = true
    hasError = false

    /*Sorting */
    sort = TOKEN_FILTER_VALUES[0]

    /*
    ===================================================================================
      Computed
    ===================================================================================
    */
    get tokenOptions() {
        const options = [
            {
                value: TOKEN_FILTER_VALUES[0],
                text: this.$i18n.tc('token.name', 1),
                filter: this.$i18n.t('filter.low')
            },
            {
                value: TOKEN_FILTER_VALUES[1],
                text: this.$i18n.tc('token.name', 1),
                filter: this.$i18n.t('filter.high')
            },
            {
                value: TOKEN_FILTER_VALUES[2],
                text: this.isERC20 ? this.$i18n.t('common.amount') : this.$i18n.t('common.id'),
                filter: this.$i18n.t('filter.low')
            },
            {
                value: TOKEN_FILTER_VALUES[3],
                text: this.isERC20 ? this.$i18n.t('common.amount') : this.$i18n.t('common.id'),
                filter: this.$i18n.t('filter.high')
            }
        ]

        if (this.isERC20) {
            options.push(
                {
                    value: TOKEN_FILTER_VALUES[4],
                    text: this.$i18n.t('usd.value'),
                    filter: this.$i18n.t('filter.low')
                },
                {
                    value: TOKEN_FILTER_VALUES[5],
                    text: this.$i18n.t('usd.value'),
                    filter: this.$i18n.t('filter.high')
                },
                {
                    value: TOKEN_FILTER_VALUES[6],
                    text: this.$i18n.t('token.change'),
                    filter: this.$i18n.t('filter.low')
                },
                {
                    value: TOKEN_FILTER_VALUES[7],
                    text: this.$i18n.t('token.change'),
                    filter: this.$i18n.t('filter.high')
                }
            )
        }
        return options
    }

    get loading(): boolean {
        if (this.isNFT) {
            return this.initialLoad || this.loadingMeta
        }
        return this.initialLoad
    }

    get tokens(): Token[] {
        const start = this.index * this.maxItems
        if (!this.loading && this.hasTokens && this.tokenSort) {
            const end = start + this.maxItems > this.getTokens.length ? this.getTokens.length : start + this.maxItems
            return this.tokenSort.getSortedTokens(this.sort).slice(start, end)
        }
        return []
    }
    /**
     * Gets an object with all sorted arrays
     *
     * @returns false OR Map<string, TokenMarketData>  if values have been loaded
     * @returns  null  otherwise
     */
    get tokenPrices(): Map<string, TokenMarketData> | false | null {
        if (!this.loading && this.isERC20) {
            const contracts: string[] = []
            this.getTokens.forEach(token => {
                contracts.push(token.tokenInfo.contract)
            })
            if (contracts.length > 0) {
                return this.getEthereumTokensMap(contracts)
            }
        }
        return null
    }

    get message(): string {
        if (!this.loading && this.hasTokens && this.getTokens.length === 0) {
            if (this.isNFT) {
                return `${this.$t('message.token.no-nft-addr')}`
            }
            return `${this.$t('message.token.no-tokens-addr')}`
        }
        return ''
    }

    get getTitle(): string {
        return this.isERC20 ? `${this.$t('token.erc20')}` : `${this.$t('token.nft')}`
    }

    get hasTokens(): boolean {
        return this.getTokens && this.getTokens != null
    }

    get isERC20(): boolean {
        return this.tokenType === 'ERC20'
    }

    get isNFT(): boolean {
        return this.tokenType === 'ERC721'
    }

    get eventType(): AddressEventType {
        return this.isERC20 ? AddressEventType.NEW_ERC20_TRANSFER : AddressEventType.NEW_ERC721_TRANSFER
    }
    get updateText(): string {
        return `${this.$t('message.update.tokens')}`
    }

    /**
     * Gets an object with all sorted arrays
     * @param {String} contract
     * @returns
     * - TokenSort if all has been loaded
     * - false if still loading
     */

    get tokenSort(): TokenSort | false {
        if (!this.loading && this.getTokens && this.isERC20 && this.tokenPrices !== null) {
            return new TokenSort(this.getTokens, this.tokenPrices, false, this.isERC20)
        }
        if (!this.loading && this.getTokens && !this.isERC20) {
            return new TokenSort(this.getTokens, false, this.nftMeta, this.isERC20)
        }
        return false
    }

    /*
    ===================================================================================
      Methods:
    ===================================================================================
    */

    sortTokens(sort: string): void {
        this.sort = sort
    }
    /**
     * Sets page or reset
     * @params page {Number} reset {Boolean}
     */
    setPage(page: number, reset: boolean = false): void {
        if (reset) {
            this.$emit('resetUpdateCount', this.eventType, true)
        } else {
            this.index = page
        }
    }
    /**
     * Fetches more values
     * @param nextKey {String}
     */
    async fetchMore(nextKey: string): Promise<boolean> {
        try {
            await this.$apollo.queries.getTokens.fetchMore({
                variables: {
                    hash: this.address,
                    _nextKey: nextKey
                },
                updateQuery: (previousResult, { fetchMoreResult }) => {
                    const newT = fetchMoreResult.getOwnersERC20Tokens.owners
                    const prevT = previousResult.getOwnersERC20Tokens.owners
                    return {
                        getOwnersERC20Tokens: {
                            __typename: previousResult.getOwnersERC20Tokens.__typename,
                            owners: [...prevT, ...newT],
                            nextKey: fetchMoreResult.getOwnersERC20Tokens.nextKey
                        }
                    }
                }
            })
            return true
        } catch (e) {
            const newE = JSON.stringify(e)
            if (!newE.toLowerCase().includes(excpInvariantViolation)) {
                throw new Error(newE)
            }
            return false
        }
    }
    /* NFT TOKENS*/
    /**
     * Displays NFT tokens
     * @param contract {String} name {String}
     */
    showNftTokens(contract: string, name: string | null): void {
        this.requestContractName = name ? name : contract
        if (contract) {
            if (this.uniqueNFTMap && this.uniqueNFTMap[contract]) {
                this.requestContract = contract
                this.uniqueNFT = this.uniqueNFTMap[contract]
                this.loadingUniqueNFT = false
                this.showUniqueNFT = true
            } else {
                /* Load Tokens */
                this.requestContract = contract
                this.showUniqueNFT = true
                this.loadingUniqueNFT = true
                this.skipGetUniqueTokens = false
            }
        }
    }
    /**
     * Fetches more NFT tokens for the address
     */
    async fetchMoreUniqueNFT(): Promise<boolean> {
        try {
            await this.$apollo.queries.getTokens.fetchMore({
                variables: {
                    hash: this.address,
                    _nextKey: this.getOwnersERC721Tokens.nextKey
                },
                updateQuery: (previousResult, { fetchMoreResult }) => {
                    const newT = fetchMoreResult.getOwnersERC721Tokens
                    const prevT = previousResult.getOwnersERC721Tokens
                    return {
                        getOwnersERC721Tokens: {
                            __typename: previousResult.getOwnersERC721Tokens.__typename,
                            tokens: [...prevT, ...newT],
                            nextKey: fetchMoreResult.getOwnersERC721Tokens.nextKey
                        }
                    }
                }
            })
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
     * Hides NFT tokens
     */
    hideNFTTokens(): void {
        this.showUniqueNFT = false
        this.loadingUniqueNFT = true
    }
    /**
     * Emit error to Sentry
     * @param val {Boolean}
     */
    emitErrorState(val: boolean): void {
        this.hasError = val
        const errorType = this.isERC20 ? ErrorMessage.tokensERC20 : ErrorMessage.tokens721
        this.$emit('errorTokenBalance', this.hasError, errorType)
    }
    /**
     * Creates a meta Map for the NFTS
     */
    createNFTMetaMap(): void {
        const contracts = this.getNFTcontractsMeta ? this.getNFTcontractsMeta.tokenContracts : null
        if (contracts && contracts.length > 0) {
            contracts.forEach(contract => {
                if (contract && contract.primary_asset_contracts) {
                    contract.primary_asset_contracts.forEach(asset => {
                        this.nftMeta[asset.address] = asset
                    })
                }
            })
        }
        this.loadingMeta = false
    }
    /**
     * Fetches NFT Contract meta
     * @param contract {String}
     */
    getContractMeta(contract: string): NFTMetaType | undefined {
        if (this.isNFT) {
            return this.nftMeta[contract]
        }
        return undefined
    }

    /*
    ===================================================================================
      Watch
    ===================================================================================
    */
    @Watch('refetchTokens')
    onRefetchTokensChanged(newVal: boolean, oldVal: boolean): void {
        if (newVal && newVal !== oldVal) {
            if (this.isERC20) {
                this.$emit('loadingERC20Tokens', true)
            }
            if (this.isNFT) {
                this.loadingMeta = true
                this.$apollo.queries.getNFTcontractsMeta.refetch()
            }
            this.index = 0
            this.initialLoad = true
            this.totalPages = 0
            this.showPagination = false
            this.$apollo.queries.getTokens
                .refetch()
                .then(data => {
                    if (data) {
                        this.$emit('resetBalanceRefetch')
                    }
                })
                .catch(error => {
                    this.emitErrorState(true)
                    throw error
                })
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
