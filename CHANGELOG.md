### Bug

- Fix pgainate has more bug for the last page [#953](https://github.com/EthVM/EthVM/pull/953)

### Devop

- Refactor Pagination components and update UI [#953](https://github.com/EthVM/EthVM/pull/953)

===================================================================================

### Release v1.0.6

### Bug

- Prevent searching an empty string & remove onlyLetters [#944](https://github.com/EthVM/EthVM/pull/944)
- Fix Cannot read property 'insertBefore' of null in Address ERC20 Tokens [#945](https://github.com/EthVM/EthVM/pull/945)
- Add Invalid Hash error to exceptions [#933](https://github.com/EthVM/EthVM/pull/933)
- Add loader for isContract [#939](https://github.com/EthVM/EthVM/pull/939)
- Fix Cannot read property 'to' of null, sentry 2096719583 [#948](https://github.com/EthVM/EthVM/pull/948)
- Do checksum on valid addresses only [#949](https://github.com/EthVM/EthVM/pull/949)
- Fixed x and y are too far apart with stepSize of 1 minute [#947](https://github.com/EthVM/EthVM/pull/947)
- Fixed 'Cannot set property 'font' of null' [#947](https://github.com/EthVM/EthVM/pull/947)
- Cannot read property 'skip' of undefined [#947](https://github.com/EthVM/EthVM/pull/947)
- Corrected data points within unique charts for duration [#947](https://github.com/EthVM/EthVM/pull/947)
- Fixed 'From timestamp is larger than to timestamp' in uniqe charts [#947](https://github.com/EthVM/EthVM/pull/947)
- Fix invariant violation in fetchMore. [#936](https://github.com/EthVM/EthVM/pull/936)
- Fix old name is shown in edit/remove fav address dialog , after change [#950](https://github.com/EthVM/EthVM/pull/950)

### Devop

- Bump ini from 1.3.5 to 1.3.8 [#943](https://github.com/EthVM/EthVM/pull/943)
- Refactor Tokens Sorting in Address, loading logic [#945](https://github.com/EthVM/EthVM/pull/945)
- Change Unique chart route names [#942](https://github.com/EthVM/EthVM/pull/942)
- Implemented handler for undefined results in getTimeseriesData [#947](https://github.com/EthVM/EthVM/pull/947)
- Refactor logic for home transactions chart to correct display updated data points in lines [#947](https://github.com/EthVM/EthVM/pull/947)
- remove console [#941](https://github.com/EthVM/EthVM/pull/941)
- adjust nav drawer breakpoint [#951](https://github.com/EthVM/EthVM/pull/951)

===================================================================================

### Release v1.0.5-hotfix.1

### Bug

- Switch tabs for Address Details [#935](https://github.com/EthVM/EthVM/pull/935)

===================================================================================

### Release v1.0.5

### Feature

- Favorite ERC20 tokens List. Add/Remove tokens. [#921](https://github.com/EthVM/EthVM/pull/921)

### Bug

- Fix disappearance of the header on some breakpoints [#929](https://github.com/EthVM/EthVM/pull/929)
- Fix no statediff for reg address [#926](https://github.com/EthVM/EthVM/pull/926)

### Devop

- Mobile ui adjustments of the search button on the app header [#929](https://github.com/EthVM/EthVM/pull/929)
- Removed console log and unused comments[#923](https://github.com/EthVM/EthVM/pull/923)
- Refactor known apollo exception [#922](https://github.com/EthVM/EthVM/pull/922)
- Fix sending known apollo exception to Sentry [#922](https://github.com/EthVM/EthVM/pull/922)
- Adjust blocks height to 100% on Address page [#930](https://github.com/EthVM/EthVM/pull/930)
- Added link to myetherwallet.com to 'powered by mew' image [#928](https://github.com/EthVM/EthVM/pull/928)
- Fixed incorrect ui layout on fav address page and fav address row mobile [#927](https://github.com/EthVM/EthVM/pull/927)

===================================================================================

### Release v1.0.4

### Feature

- Sort Address Tokens [#909](https://github.com/EthVM/EthVM/pull/909)
- Add in Address Contract Details [#914](https://github.com/EthVM/EthVM/pull/914)

### Bug

- Query failed tx state diff if null [#913](https://github.com/EthVM/EthVM/pull/913)
- NFT ids not truncating on address and token details page [#918](https://github.com/EthVM/EthVM/pull/918)

### Devop

- Remove "To" on contract creation on Eth Transfers on Address Transfers and Transactions Table [#903](https://github.com/EthVM/EthVM/pull/903)
- Contract creation [#912](https://github.com/EthVM/EthVM/pull/912)
- Enforce branch names [#911](https://github.com/EthVM/EthVM/pull/911)
- Create workflow for master and staging [#916](https://github.com/EthVM/EthVM/pull/916)

===================================================================================

### Release v1.0.3-hotfix.2

- Fix variables [#908](https://github.com/EthVM/EthVM/pull/908)

===================================================================================

### Release v1.0.3-hotfix.1

### Devop

- Removed individual subscriptions for each pending transactions on address and pending txs page. [#905](https://github.com/EthVM/EthVM/pull/905)
- Added identicon to address QR dialog [#906](https://github.com/EthVM/EthVM/pull/906)
- Set correct DSN for Sentry [#904](https://github.com/EthVM/EthVM/pull/904)
- Clean up packages, install depcheck, config husky with commitlint [#882](https://github.com/EthVM/EthVM/pull/882)

### Bug

- Fixed mined blinker on pending txs table on pending txs page. [#905](https://github.com/EthVM/EthVM/pull/905)
- Fixed Unclickable QR on mobile [#906](https://github.com/EthVM/EthVM/pull/906)

===================================================================================

### Release v1.0.3

### Feature

- Favorite Address List. Add/Remove/Edit Name of the address, live balance update within a list. [#867](https://github.com/EthVM/EthVM/pull/867)

### Devop

- Use address provided in footer [#879](https://github.com/EthVM/EthVM/pull/879)
- Change token sort arrow ui [#890](https://github.com/EthVM/EthVM/pull/890)

### Bug

- Deep watch on chart data sets [#885](https://github.com/EthVM/EthVM/pull/885)
- Fixed transform hash component for easy string copy [#899](https://github.com/EthVM/EthVM/pull/899/commits/e63c11860e9b0a22d7cfecad80719c8787e2c42c)
- Fix mobile loading state padding for address header page [#891](https://github.com/EthVM/EthVM/pull/891)
- Change 24 price change to 24 price change percentage [#887](https://github.com/EthVM/EthVM/pull/887)
- Sentry fix #1950193750 and set contract correctly in CoinData [#895](https://github.com/EthVM/EthVM/pull/895)
- Catch v-img errors on load [#881](https://github.com/EthVM/EthVM/pull/881)

===================================================================================

### Release v1.0.2

### Bug

- Fix state diff types [#873](https://github.com/EthVM/EthVM/pull/873)
- Fix chart font bug [#869](https://github.com/EthVM/EthVM/pull/869)

### Devop

- Fix public path [#878](https://github.com/EthVM/EthVM/pull/878)
- Update ethvm builds action [#877](https://github.com/EthVM/EthVM/pull/877)
- Add version to footer [#872](https://github.com/EthVM/EthVM/pull/872)
- Add sentry environment [#871](https://github.com/EthVM/EthVM/pull/871)
- Convert all address instance to checksum [#870](https://github.com/EthVM/EthVM/pull/870)
- Fixed spelling mistakes in readme [#866](https://github.com/EthVM/EthVM/pull/866)
- Removedd hardocded strings from UI token details page [#875](https://github.com/EthVM/EthVM/pull/875)
- replaced router-link-tag with a-tag [#880](https://github.com/EthVM/EthVM/pull/880)

===================================================================================

### Release v1.0.1

### Feature

- Detect if address is contract [#857](https://github.com/EthVM/EthVM/pull/857)

### Bug

- Catch correct error [#860](https://github.com/EthVM/EthVM/pull/860)
- Detect if address is contract [#957](https://github.com/EthVM/EthVM/pull/957)
- Add pending tx table [#833](https://github.com/EthVM/EthVM/pull/833)
- Fix BLOCKS pagination is not taking block 0 into account [#850](https://github.com/EthVM/EthVM/pull/850)
- Only hide filter on initialLoad [#848](https://github.com/EthVM/EthVM/pull/848)
- Fix Search All by address by adding isValidAddress check [#847](https://github.com/EthVM/EthVM/pull/847)
- Fix block transaction if route param of the block details is hash [#851](https://github.com/EthVM/EthVM/pull/851)
- Fix block parent route changed to hash [#851](https://github.com/EthVM/EthVM/pull/851)
- Fix for item padding within details list [#853](https://github.com/EthVM/EthVM/pull/853)
- Fix animation for sum menu item change on address page [#854](https://github.com/EthVM/EthVM/pull/854)
- Fix strings [#852](https://github.com/EthVM/EthVM/pull/852)
- Fixes of incorrect rendering of the menu items when user slides between tab items and on switch between mobile/desktop layouts [#855](https://github.com/EthVM/EthVM/pull/855)
- Block Number set to update only when the block number is higher [#835](https://github.com/EthVM/EthVM/pull/835)

### Devop

- New dark layout for mobile tabs
- Added skip query for Contract Meta
- Added error state to emitError on Balance
- Additional animation fix
- Updated NFT image api route
- Added coursor to mobile tab menu, hover color change
- Added divider to app details list for tokens and block
