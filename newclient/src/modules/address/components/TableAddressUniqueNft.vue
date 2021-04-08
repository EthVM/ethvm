<template>
    <div>
        <!--
    =====================================================================================
      TABLE HEADER FOR UNIQUE TOKENS
    =====================================================================================
    -->
        <v-layout align-center justify-space-around row wrap fill-height pr-2 pl-2>
            <v-flex shrink>
                <v-btn flat color="primary" class="text-capitalize ma-0" @click="$emit('hideNFT')">
                    <v-icon left small>fas fa-arrow-left</v-icon>{{ $t('btn.back') }}</v-btn
                >
            </v-flex>
            <v-flex xs7 sm6 md5>
                <v-card-title class="title font-weight-bold pa-0">{{ contractName }} </v-card-title>
            </v-flex>
            <v-spacer />
            <v-flex shrink>
                <app-paginate v-if="showPagination" :total="totalPages" :current-page="index" @newPage="setPage" />
            </v-flex>
        </v-layout>
        <v-layout v-if="!loading" align-center justify-start row wrap fill-height pr-2 pl-2>
            <v-flex v-for="(token, i) in uniqueTokens" :key="i" xs12 sm6 md4>
                <unique-nft-row :token="token" :loading="false" :contract="contract" />
            </v-flex>
        </v-layout>
        <v-layout v-else align-center justify-start row wrap fill-height pr-2 pl-2>
            <v-flex v-for="i in 4" :key="i" xs12 sm6 md4>
                <unique-nft-row :loading="true" :contract="contract" />
            </v-flex>
        </v-layout>
    </div>
</template>

<script lang="ts">
import { Component, Prop, Vue, Watch } from 'vue-property-decorator'
import AppPaginate from '@app/core/components/ui/AppPaginate.vue'
import UniqueNftRow from './UniqueNftRow.vue'
import { getOwnersERC721Tokens_getOwnersERC721Tokens_tokens as ERC721TokenType } from '@app/modules/address/handlers/AddressTokens/apolloTypes/getOwnersERC721Tokens'
import BN from 'bignumber.js'

const MAXTOKENS = 16
@Component({
    components: {
        AppPaginate,
        UniqueNftRow
    }
})
export default class TableAddressUniqueNft extends Vue {
    /*
    ===================================================================================
      Props
    ===================================================================================
    */
    @Prop(String) contractName!: string
    @Prop(Array) tokens!: ERC721TokenType[]
    @Prop(Boolean) loading!: boolean
    @Prop(String) contract!: string
    @Prop({ type: String, default: '' }) contractDefaultImage!: string

    /*
    ===================================================================================
      Initial Data
    ===================================================================================
    */
    index = 0

    /*
    ===================================================================================
     Computed
    ===================================================================================
    */
    get uniqueTokens(): ERC721TokenType[] {
        if (!this.loading) {
            const start = this.index * MAXTOKENS
            const end = start + MAXTOKENS > this.tokens.length ? this.tokens.length : start + MAXTOKENS
            return this.tokens.slice(start, end)
        }
        return []
    }

    get totalPages(): number {
        return this.loading ? 0 : Math.ceil(new BN(this.tokens.length).div(MAXTOKENS).toNumber())
    }
    get showPagination(): boolean {
        return this.totalPages > 1
    }

    /*
    ===================================================================================
     Methods
    ===================================================================================
    */
    /**
     * Sets page number
     * @param page {Number}
     */
    setPage(page: number): void {
        this.index = page
    }
}
</script>
<style scoped lang="css">
.token-container {
    border: 1px solid #b4bfd2;
    border-radius: 5px;
}
</style>
