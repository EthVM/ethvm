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
                  Input Container
                =====================================================================================
                -->
                <div class="pb-1 page-input">
                    <v-text-field
                        v-model="pageDisplay"
                        :mask="inputMask"
                        :placeholder="pageDisplay"
                        :error="!isValidPageDisplay"
                        :class="[validClass, inputWidthClass, 'centered-input']"
                    />
                </div>
                <!--
                =====================================================================================
                  Total Pages Text
                =====================================================================================
                -->
                <v-tooltip v-if="hasTotalTooltip" color="white" content-class="tooltip-border" top>
                    <template #activator="{on}">
                        <p class="info--text text-center total-p caption pl-1" v-on="on">{{ showText }}</p>
                    </template>
                    <span class="black--text">{{ $t('message.page') }} {{ hasTotalTooltip }}</span>
                </v-tooltip>
                <p v-else class="info--text text-center total-p caption pl-1">{{ showText }}</p>
                <!--
                =====================================================================================
                  Next Item Button
                =====================================================================================
                -->
                <v-btn :disabled="currentPage === lastPage" flat class="bttnGrey info--text text-capitalize bttn" small @click="setPageOnClick('next')"
                    ><v-icon class="secondary--text fas fa-angle-right" small />
                </v-btn>
                <!--
                =====================================================================================
                  Last Item Button
                =====================================================================================
                -->
                <v-btn :disabled="currentPage === lastPage" flat class="bttnGrey info--text text-capitalize bttn caption" small @click="setPageOnClick('last')"
                    ><v-icon class="secondary--text fas fa-angle-double-right" small />
                </v-btn>
            </v-layout>
        </v-container>
    </v-card>
</template>

<script lang="ts">
import { Component, Prop, Mixins } from 'vue-property-decorator'
import { NumberFormatMixin } from '@app/core/components/mixins/number-format.mixin'
import BigNumber from 'bignumber.js'

@Component
export default class AppPaginate extends Mixins(NumberFormatMixin) {
    /*
  ===================================================================================
    Props
  ===================================================================================
  */
    @Prop(Number) total!: number
    @Prop(Number) currentPage!: number

    /*
  ===================================================================================
    Initial Data
  ===================================================================================
  */
    validClass = 'center-input body-1 secondary--text'
    invalidClass = 'center-input body-1 error--text'
    isError = false
    pageDisplayUpdateTimeout: number | null = null // Timeout object to update page with override of pageDisplay input model

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
     * If desired page is within valid page range, emit new page.
     *
     * @param {Number} page - Desired page to traverse
     */
    setPage(page: number) {
        if (this.isValidPage(page) && page !== this.currentPage) {
            this.emitNewPage(page)
        }
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
                this.emitNewPage(Math.min(this.lastPage, this.currentPage + 1))
                break
            case 'last':
                this.emitNewPage(this.lastPage)
                break
            default:
                break
        }
    }

    /**
     * Determine if a given @number is within the valid page range.
     *
     * @page {Number} - Page number to be validated
     * @return {Boolean}
     */
    isValidPage(page: number): boolean {
        return page >= 0 && page <= this.lastPage
    }

    /*
  ===================================================================================
    Set Values
  ===================================================================================
  */

    set pageDisplay(pageDisplay: string) {
        const desiredPage = parseInt(pageDisplay, 10) - 1
        ;(desiredPage >= 0 && desiredPage <= this.lastPage) || !pageDisplay ? (this.isError = false) : (this.isError = true)
        if (this.pageDisplayUpdateTimeout) {
            clearTimeout(this.pageDisplayUpdateTimeout)
        }
        this.pageDisplayUpdateTimeout = window.setTimeout(() => {
            this.setPage(desiredPage)
        }, 1000)
    }

    /*
  ===================================================================================
    Computed Values
  ===================================================================================
  */

    /**
     * Returns Text for the total pages near the input
     *
     * @returns {string}
     */
    get showText(): string {
        return `${this.$t('message.page')} ${this.formatIntegerValue(new BigNumber(this.total), true).value}`
    }

    /**
     * Transform the "zero-based" value of this.page into
     * a human-readable string that starts from 1 as opposed to 0
     * @returns {string}
     */
    get pageDisplay(): string {
        return new BigNumber(this.currentPage + 1).toFixed()
    }

    /**
     * Determine if an given @number is within the valid page range.
     *
     * @return {Boolean}
     */
    get isValidPageDisplay(): boolean {
        return !this.isError
    }

    /**
     * Display tooltip if totalPages >= 1k
     *
     * @return {string | undefined} - string IF totalPages >= 1k, undefined otherwise
     */
    get hasTotalTooltip(): string | undefined {
        return this.total >= 1e3 ? this.formatNumber(this.total) : undefined
    }

    /**
     * Property this.total is a human-readable number/length as opposed to a zero-based number.
     * The last possible page is zero-based, so this translates the human-readable number into zero-based
     *
     * @returns {number}
     */
    get lastPage(): number {
        return this.total - 1
    }

    get inputMask(): string {
        let mask = '#'
        while (mask.length != this.total.toString().length) {
            mask += '#'
        }
        return mask
    }

    /**
     * Returns Class name of the input width
     * Determines width of the input accordign to the total page size
     *
     * @returns {string}
     */
    get inputWidthClass(): string {
        if (this.total.toString().length < 3) {
            return 'x-sm'
        }
        if (this.total.toString().length < 6) {
            return 'sm'
        }
        if (this.total.toString().length < 9) {
            return 'md'
        }

        return 'lg'
    }
}
</script>

<style scoped lang="scss">
.v-btn {
    height: 30px;
    min-width: 20px;
    margin: 5px;
}

.x-sm {
    max-width: 2em;
}
.sm {
    max-width: 3em;
}
.md {
    max-width: 5em;
}
.lg {
    max-width: 8em;
}

p {
    margin: 0;
    padding: 0;
}
.total-p {
    white-space: nowrap;
}
</style>
