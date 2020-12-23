<template>
    <chart
        :chart-title="title"
        :chart-description="description"
        :datacollection="chartData"
        :chart-options="chartOptions"
        :show-balance-options="true"
        @timeFrame="setTimeFrame"
    />
</template>

<script lang="ts">
import { Component, Prop, Watch, Mixins } from 'vue-property-decorator'
import Chart from '@app/modules/charts/components/Chart.vue'
import { ChartDataMixin } from '@app/modules/charts/mixins/ChartDataMixin.mixin'
import {
    TimeseriesKey,
    DataPoint,
    ChartData,
    Dataset,
    ChartOptions,
    TimeseriesValue,
    TimeseriesValueKey,
    TimeOptions,
    ComponentDataInterface
} from '@app/modules/charts/models'
import { TimeseriesScale } from '@app/apollo/global/globalTypes'
import { getTimeseriesData_getTimeseriesData as GetTimeseriesDataType } from '@app/modules/charts/handlers/apolloTypes/getTimeseriesData'
import { getTimeseriesData, timeseriesEthAvg } from '@app/modules/charts/handlers/timeseriesData.graphql'
import { Footnote } from '@app/core/components/props'
import moment from 'moment'

/**
 * Defualt values time frame options
 * 0 === By Day Last Hour, return 60 data points for the last hour in minutes.
 * 1 === By Hour Last 24 hours, return 24 data points for the last day, 1 point per hour
 * 2 === By Minute Last Week, return 168 data points for the last week , 1 point per hours
 * 3 === Last Month, return 30 data points for the last week, 1 point per day
 * 4 === Last 6th Months, return 180 data points for the last 6 months, 1 point per day
 * 5 === Last Year, returns 365 data points for the last year, 1 point per day
 * NOTE: missing all functionality.
 */

interface qVars {
    id: string
    scale: TimeseriesScale
    start: number
    max_items: number
    timeOptions: {
        unit: string
        displayFormats: {
            minute: string
        }
        stepSize: number
    }
}

const DEFAULT_DATA = {
    0: {
        id: 'day',
        scale: TimeseriesScale.days,
        timeOptions: {
            unit: 'day',
            displayFormats: {
                day: 'ddd, h:mm a'
            },
            stepSize: 1
        }
    },
    1: {
        id: 'hour',
        scale: TimeseriesScale.hours,
        timeOptions: {
            unit: 'minute',
            displayFormats: {
                minute: 'ddd, h:mm a'
            },
            stepSize: 1
        }
    },
    2: {
        id: 'min',
        scale: TimeseriesScale.hours,
        timeOptions: {
            unit: 'day',
            displayFormats: {
                day: 'ddd, h:mm a'
            },
            stepSize: 1
        }
    }
}

@Component({
    components: {
        Chart
    },
    apollo: {
        getChartData: {
            query: getTimeseriesData,
            variables() {
                return {
                    key: this.key,
                    scale: this.scale,
                    fromT: this.creationTimestamp,
                    toT: this.endTimestamp
                }
            },
            update: data => (data.getTimeseriesData ? data.getTimeseriesData.items : null),
            result({ data }) {
                this.chartDataSet = [...this.mapItemsToDataSet(data.getTimeseriesData.items, this.value_type)]
            }
        }
    }
})
export default class TimeSeriesChartData extends Mixins(ChartDataMixin) {
    /*
    ===================================================================================
     Initial Data
    ===================================================================================
    */

    getChartData!: GetTimeseriesDataType
    chartDataSet: DataPoint[] = []
    scale = TimeseriesScale.days
    maxItems = 5
    start = 10
    timeOptions = DEFAULT_DATA[0].timeOptions
    creationTimestamp = 1602667372
    endTimestamp = 1606219200

    nextKey = null

    key = 'ACCOUNT_BALANCE_PREFIX_AVG-0xETH-0x00000000219ab540356cbb839cbe05303d7705fa'

    /*
    ===================================================================================
      Computed
    ===================================================================================
    */

    get title(): string {
        return 'Eth2 Deposit Address Balance'
    }
    get description(): string {
        return 'Eth2 Deposit Address Balance breakdown from first deposit on Nov 3, 2020 to the deadline Nov 24, 2020'
    }

    // get value_type(): TimeseriesValue {
    //     return TimeseriesValueKey[this.chartKey]
    // }

    get chartData(): ChartData | null {
        const _datasets: Dataset[] = []
        if (this.chartDataSet) {
            _datasets.push({
                data: this.chartDataSet,
                label: 'Balance(ETH)',
                backgroundColor: 'rgba(118, 221, 251, 0.2)',
                borderColor: '#2c82be',
                borderWidth: 2
            })
        }
        if (_datasets.length > 0) {
            return {
                datasets: _datasets
            }
        }
        return null
    }
    get chartOptions(): ChartOptions {
        return {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                xAxes: [
                    {
                        type: 'time',
                        display: true,
                        time: this.timeOptions,
                        scaleLabel: {
                            display: false,
                            labelString: 'Time'
                        },
                        ticks: {
                            beginAtZero: false,
                            source: 'auto',
                            callback: function (dataLabel, index) {
                                return index % 2 === 0 ? dataLabel : ''
                            }
                        }
                    }
                ],
                yAxes: [
                    {
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: `Balance(ETH)`
                        },
                        ticks: {
                            source: 'auto',
                            callback: function (dataLabel, index) {
                                return index % 2 === 0 ? dataLabel : ''
                            }
                        }
                    }
                ]
            }
        }
    }

    /*
    ===================================================================================
      Methods
    ===================================================================================
    */
    /**
     * Sets time frame for chart
     * @param value {Number}
     */
    setTimeFrame(value: number): void {
        this.scale = DEFAULT_DATA[value].scale
        this.maxItems = DEFAULT_DATA[value].max_items
        this.start = DEFAULT_DATA[value].start
        this.timeOptions = DEFAULT_DATA[value].timeOptions
        this.$apollo.queries.getChartData.refetch()
    }

    /**
     * Fetches more values
     * @param nextKey {String}
     */
    // fetchMore(nextKey: string): void {
    //     this.$apollo.queries.getChartData.fetchMore({
    //         variables: {
    //             hash: this.address,
    //             _nextKey: nextKey
    //         },
    //         updateQuery: (previousResult, { fetchMoreResult }) => {
    //             const newT = fetchMoreResult.getOwnersERC20Tokens.owners
    //             const prevT = previousResult.getOwnersERC20Tokens.owners
    //             return {
    //                 getOwnersERC20Tokens: {
    //                     __typename: previousResult.getOwnersERC20Tokens.__typename,
    //                     owners: [...prevT, ...newT],
    //                     nextKey: fetchMoreResult.getOwnersERC20Tokens.nextKey
    //                 }
    //             }
    //         }
    //     })
    // }
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
