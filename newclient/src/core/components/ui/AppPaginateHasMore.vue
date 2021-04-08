<template>
    <v-card color="transparent" flat max-width="340">
        <v-container grid-list-xs pa-1>
            <v-layout row align-center justify-end fill-height>
                <!--
                =====================================================================================
                  First Item Button
                =====================================================================================
                -->
                <v-btn :disabled="currentPage === 0" flat class="bttnGrey info--text text-capitalize bttn" small @click="setPageOnClick('first')"
                    ><v-icon class="secondary--text fas fa-angle-double-left" small />
                </v-btn>
                <!--
                =====================================================================================
                  Prev Item Button
                =====================================================================================
                -->
                <v-btn :disabled="currentPage === 0" flat class="bttnGrey info--text text-capitalize bttn" small @click="setPageOnClick('prev')"
                    ><v-icon class="secondary--text fas fa-angle-left" small />
                </v-btn>
                <!--
                =====================================================================================
                  Current Pages Text
                =====================================================================================
                -->
                <p class="info--text pr-1">{{ textDisplay }}</p>
                <!--
                =====================================================================================
                  Next Item Button
                =====================================================================================
                -->
                <v-btn :disabled="disableNext" flat class="bttnGrey info--text text-capitalize bttn" small @click="setPageOnClick('next')"
                    ><v-icon class="secondary--text fas fa-angle-right" small />
                </v-btn>
                <!--
                =====================================================================================
                  Last Item Button
                =====================================================================================
                -->
                <v-btn v-if="hasLast" :disabled="disableNext" flat class="bttnGrey info--text text-capitalize bttn" small @click="setPageOnClick('last')"
                    ><v-icon class="secondary--text fas fa-angle-double-right" small />
                </v-btn>
            </v-layout>
        </v-container>
    </v-card>
</template>

<script lang="ts">
import { Component, Mixins, Prop, Watch } from 'vue-property-decorator'
import { NumberFormatMixin } from '@app/core/components/mixins/number-format.mixin'
import BigNumber from 'bignumber.js'

@Component
export default class AppPaginate extends Mixins(NumberFormatMixin) {
    /*
  ===================================================================================
    Props
  ===================================================================================
  */

    @Prop(Boolean) hasMore!: boolean
    @Prop(Number) currentPage!: number
    @Prop(Boolean) loading!: boolean

    /*
  ===================================================================================
    Initial Data
  ===================================================================================
  */

    hasLast = false
    lastPageLoad = 0

    /*
  ===================================================================================
    Methods
  ===================================================================================
  */

    /**
     * Emit event to parent compoent/view with updated page number.
     *
     * @param  {Number} - Page to emit to parent
     */
    emitNewPage(page: number) {
        this.$emit('newPage', page)
    }

    /**
     * On pagination button click, emit updated page number to parent component/view
     *
     * @param {String} value - Name of action to perform
     */
    setPageOnClick(value: string): void {
        switch (value) {
            case 'first':
                this.emitNewPage(0)
                break
            case 'prev':
                this.emitNewPage(Math.max(0, this.currentPage - 1))
                break
            case 'next':
                this.emitNewPage(this.currentPage + 1)
                break
            case 'last':
                this.emitNewPage(this.lastPageLoad)
                break
            default:
                break
        }
    }

    /*
  ===================================================================================
    Computed Values
  ===================================================================================
  */

    /**
     * Transform the "zero-based" value of this.page into
     * a human-readable string that starts from 1 as opposed to 0
     */
    get textDisplay(): string {
        return this.formatIntegerValue(new BigNumber(this.currentPage + 1)).value
    }

    /**
     * NEXT Button: Disabled  when content is loading and on last page when all content has been loaded
     * LAST Button: Button is present when all pages has been loaded. Button is disabled when lastPageLoad is equal to the current Page
     * @return - boolean
     */
    get disableNext(): boolean {
        if (this.loading) {
            return true
        }
        if (this.hasLast) {
            return this.currentPage === this.lastPageLoad
        }
        return false
    }

    /*
  ===================================================================================
   Watch
  ===================================================================================
  */
    @Watch('currentPage')
    onCurrentPageChanged(newVal: number, oldVal: number): void {
        if (newVal > this.lastPageLoad && newVal > oldVal) {
            this.lastPageLoad = this.currentPage
        }
    }

    @Watch('hasMore')
    onHasMoreChanged(newVal: boolean, oldVal: boolean): void {
        if (!newVal && oldVal) {
            this.hasLast = true
        }
    }
}
</script>

<style scoped lang="css">
.v-btn {
    height: 30px;
    min-width: 20px;
    margin: 5px;
}

.page-input {
    width: 80px;
}

p {
    margin: 0;
    padding: 0;
}
</style>
