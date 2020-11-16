<template>
    <router-link v-if="link" :to="link" :class="hashClass">
        <span class="firstPart">{{ start }}</span>
        <span class="lastPart">{{ end }}</span>
    </router-link>
    <div v-else :class="hashClass">
        <span class="firstPart">{{ start }}</span
        ><span class="lastPart">{{ end }}</span>
    </div>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator'

@Component
export default class AppTransformHash extends Vue {
    /*
    ===================================================================================
      Props
    ===================================================================================
    */
    @Prop(String) hash!: string
    @Prop({ type: Boolean, default: false }) italic!: boolean
    @Prop(String) link?: string
    @Prop({ type: Boolean, default: true }) isBlue!: boolean

    /*
    ===================================================================================
      Computed
    ===================================================================================
    */
    get start(): string {
        const n = this.hash.length
        return this.hash.slice(0, n - 4)
    }
    get end(): string {
        const n = this.hash.length
        return this.hash.slice(n - 4, n)
    }
    get hashClass(): string {
        const base = this.link ? 'hash-container font-mono url' : ' hash-container font-mono'
        return this.isBlue ? `${base} secondary--text` : `${base} black--text`
    }

    /*
    ===================================================================================
      Methods
    ===================================================================================
    */
    routeTo(): void {
        if (this.link) {
            this.$router.push(this.link).catch(() => {})
        }
    }
}
</script>

<style scoped lang="scss">
// Variables to control the truncation behaviour
$startFixedChars: 4; // Number of chars before ellipsis - have priority over end chars
$endFixedChars: 5; // Number of chars after ellipsis  - lower priority than start chars
$fontFaceScaleFactor: 0.47; // Magic number dependent on font face - set by trial and error

// Dervied from the 3 variables above
$startWidth: 1em * $fontFaceScaleFactor * ($startFixedChars + 3);
$endWidth: 1em * $fontFaceScaleFactor * $endFixedChars;

.firstPart,
.lastPart {
    display: inline-block;
    white-space: nowrap;
    overflow: hidden;
}
.firstPart {
    max-width: calc(100% - #{$endWidth});
    min-width: $startWidth;
    text-overflow: ellipsis;
}
.lastPart {
    max-width: calc(100% - #{$startWidth});
    direction: rtl;
}
.hash-container {
    white-space: nowrap;
    overflow: hidden;
    max-height: 1em;
    text-align: start;
    line-height: 1em;
}
// .url {
//     line-height: 1;
//     display: inline;
// }
</style>
