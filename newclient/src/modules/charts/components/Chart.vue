<template>
    <v-card color="white" flat class="mb-3 pa-1">
        <!--
        =====================================================================================
          Chart header
        =====================================================================================
        -->
        <v-layout align-center row wrap mb-0>
            <!--
            =====================================================================================
             Title and short description
            =====================================================================================
            -->
            <v-flex grow pa-1>
                <v-card-title class="title font-weight-bold ml-1 pb-0">{{ chartTitle }}</v-card-title>
                <v-card-text v-if="!showBalanceOptions" class="info--text caption ml-1 pt-2 pb-1">{{ description }}</v-card-text>
                <v-card-text v-else class="info--text caption ml-1 pt-2 pb-1">{{ chartDescription }}</v-card-text>
            </v-flex>
            <!--
            =====================================================================================
             Time frame buttons
            =====================================================================================
            -->
            <v-flex v-if="showTimeOptions && !showBalanceOptions" grow>
                <v-layout row wrap align-center justify-end pa-1>
                    <button :class="[toggleData === 0 ? 'active-button' : 'button']" flat small @click="setTimeFrame(0)">
                        {{ $tc('charts.states.hour', 2) }}
                    </button>
                    <button :class="[toggleData === 1 ? 'active-button' : 'button']" flat small @click="setTimeFrame(1)">
                        {{ $tc('charts.states.day', 2) }}
                    </button>
                    <button v-if="!isPending" :class="[toggleData === 2 ? 'active-button' : 'button']" flat small @click="setTimeFrame(2)">
                        {{ $tc('charts.states.week', 2) }}
                    </button>
                    <button v-if="!isPending" :class="[toggleData === 3 ? 'active-button' : 'button']" flat small @click="setTimeFrame(3)">
                        {{ $tc('charts.states.month', 2) }}
                    </button>
                    <button v-if="!isPending" :class="[toggleData === 4 ? 'active-button' : 'button']" flat small @click="setTimeFrame(4)">
                        {{ $tc('charts.states.month-six', 2) }}
                    </button>
                    <button v-if="!isPending" :class="[toggleData === 5 ? 'active-button' : 'button']" flat small @click="setTimeFrame(5)">
                        {{ $tc('charts.states.year', 2) }}
                    </button>
                    <!-- <button :class="[toggleData === 6 ? 'active-button' : 'button']" flat small @click="setTimeFrame(7)">
                        {{ $tc('charts.states.all', 2) }}
                    </button> -->
                </v-layout>
            </v-flex>
            <v-flex v-if="showBalanceOptions" grow>
                <v-layout row wrap align-center justify-end pa-1>
                    <button :class="[toggleData === 0 ? 'balance-active-button' : 'balance-button']" flat small @click="setTimeFrame(0)">
                        By Day
                    </button>
                    <button :class="[toggleData === 1 ? 'balance-active-button' : 'balance-button']" flat small @click="setTimeFrame(1)">
                        By Hour
                    </button>
                </v-layout>
            </v-flex>
        </v-layout>
        <v-divider></v-divider>
        <!--
        =====================================================================================
          Chart Body
        =====================================================================================
        -->
        <line-chart :chart-data="datacollection" :chart-options="chartOptions"></line-chart>
        <!--
        =====================================================================================
         Footnotes
        =====================================================================================
        -->
        <v-layout v-if="footnotes" align-center justify-end row fill-height pr-2> <app-footnotes :footnotes="footnotes" /> </v-layout>
    </v-card>
</template>

<script lang="ts">
import ChartJs from 'chart.js'
import LineChart from './LineChart.vue'
import AppFootnotes from '@app/core/components/ui/AppFootnotes.vue'
import AppInfoLoad from '@app/core/components/ui/AppInfoLoad.vue'
import { Footnote } from '@app/core/components/props'
import { ChartData, ChartOptions } from '@app/modules/charts/models'
import { Vue, Component, Prop, Watch } from 'vue-property-decorator'

/*
===================================================================================
  Global Chart JS settings
===================================================================================
*/

/* Font */
ChartJs.defaults.global = Object.assign(ChartJs.defaults.global, {
    defaultFontFamily: "'Roboto', 'Roboto Mono', 'sans-serif'",
    defaultFontStyle: '200'
})

/* Hide initial chart title */
ChartJs.defaults.global.title = Object.assign(ChartJs.defaults.global.title, {
    display: false
})

/* Tooltip box style */
ChartJs.defaults.global.tooltips = Object.assign(ChartJs.defaults.global.tooltips, {
    titleFontStyle: '400',
    backgroundColor: '#686868',
    mode: 'index',
    intersect: false
})

/* Hide legened */
ChartJs.defaults.global.legend = Object.assign(ChartJs.defaults.global.legend, {
    display: false
})

/* Chart layout paddings */
ChartJs.defaults.global.layout = Object.assign(ChartJs.defaults.global.layout, {
    padding: {
        top: 20,
        bottom: 10
    }
})
if (ChartJs.defaults.global.elements) {
    /* Data point style */
    ChartJs.defaults.global.elements.point = Object.assign(ChartJs.defaults.global.elements.point, {
        hoverRadius: 6,
        borderWidth: 1,
        radius: 1
    })
}

/* Show doughnut chart animation */
ChartJs.defaults.doughnut.animation = Object.assign(ChartJs.defaults.doughnut.animation, {
    animateRotate: true
})

/*
===================================================================================
  CONSTANTS
===================================================================================
*/
/**
 * @var TIME_TRANSLATION - Values time frame transaltions,
 * 0 === hour,  BY DEFAULT
 * 1 === day
 * 2 === week
 * 3 === month
 * 4 === 6th months
 * 5 === year
 * NOTE: missing all functionality (value 6).
 */
const TIME_TRANSLATION = ['hour', 'day', 'week', 'month', 'month-six', 'year']

const MAX_ITEMS = 10

@Component({
    components: {
        AppFootnotes,
        AppInfoLoad,
        LineChart
        // AppError
    }
})
export default class Chart extends Vue {
    /*
    ===================================================================================
      Props
    ===================================================================================
    */

    @Prop({ type: String }) chartTitle!: string
    @Prop({ type: String }) chartDescription!: string
    @Prop({ type: Array }) footnotes?: Footnote[]
    @Prop({ type: Object, default: null }) datacollection!: ChartData
    @Prop({ type: Object }) chartOptions!: ChartOptions
    @Prop({ type: Boolean, default: true }) showTimeOptions!: boolean
    @Prop({ type: Boolean, default: false }) isPending!: boolean
    //Balance
    @Prop({ type: Boolean, default: false }) showBalanceOptions!: boolean

    /*
    ===================================================================================
      Initial Data
    ===================================================================================
    */

    /**
     * @var toggleData - Default values time frame options
     * 0 === Last Hour  BY DEFAULT
     * 1 === Last 24 hours
     * 2 === Last Week
     * 3 === Last Month
     * 4 === Last 6th Months
     * 5 === Last Year
     * NOTE: missing all functionality (value 6).
     */

    toggleData = 0

    /* Temp Data */

    error = ''
    isLoading = true

    /*
  ===================================================================================
    Computed
  ===================================================================================
  */
    get chartClass(): string {
        const brkPoint = this.$vuetify.breakpoint.name
        switch (brkPoint) {
            case 'xs':
                return 'xs-chart'
            default:
                return ''
        }
    }
    get description(): string {
        if (!this.showTimeOptions) {
            return this.chartDescription
        }
        const time = `charts.states.${TIME_TRANSLATION[this.toggleData]}`
        return `${this.chartDescription} ${this.$t('charts.captions.other')} ${this.$tc(time, 1)}`
    }
    /*
  ===================================================================================
   Methods
  ===================================================================================
  */
    /**
     * Sets time frame and
     * emits data up to the parent
     * @param value {Number}
     */
    setTimeFrame(value: number) {
        this.toggleData = value
        this.$emit('timeFrame', this.toggleData)
    }
}
</script>

<style scoped lang="css">
.active-button {
    background-color: #3965e8;
    color: white;
    width: 32px;
    margin: 10px;
    border-radius: 2px;
    padding: 5px;
}
.button {
    color: #8391a8;
    width: 32px;
    margin: 10px;
    padding: 2px;
}

.xs-chart {
    min-height: 280px;
}

.chart-caption {
    min-height: 3em;
}

.balance-active-button {
    background-color: #3965e8;
    color: white;
    width: 100px;
    margin: 10px;
    border-radius: 2px;
    padding: 5px;
}
.balance-button {
    color: #8391a8;
    width: 100px;
    margin: 10px;
    padding: 2px;
}
</style>
