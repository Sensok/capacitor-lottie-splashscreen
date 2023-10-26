# capacitor-lottie-splashscreen

Enables lottie splash screen on Capacitor projects. Based off [CordovaLottieSplashScreen](https://github.com/timbru31/cordova-plugin-lottie-splashscreen)

## Install

```bash
npm install capacitor-lottie-splashscreen
npx cap sync
```

## API

<docgen-index>

* [`echo(...)`](#echo)
* [`hide()`](#hide)
* [`show(...)`](#show)
* [`on(...)`](#on)
* [`once(...)`](#once)
* [Type Aliases](#type-aliases)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### echo(...)

```typescript
echo(options: { value: string; }) => Promise<{ value: string; }>
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ value: string; }</code> |

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------


### hide()

```typescript
hide() => Promise<string>
```

**Returns:** <code>Promise&lt;string&gt;</code>

--------------------


### show(...)

```typescript
show(location?: string | undefined, remote?: boolean | undefined, width?: number | undefined, height?: number | undefined) => Promise<string>
```

| Param          | Type                 |
| -------------- | -------------------- |
| **`location`** | <code>string</code>  |
| **`remote`**   | <code>boolean</code> |
| **`width`**    | <code>number</code>  |
| **`height`**   | <code>number</code>  |

**Returns:** <code>Promise&lt;string&gt;</code>

--------------------


### on(...)

```typescript
on(event: LottieEvent, callback: (ev: any) => void) => void
```

| Param          | Type                                                |
| -------------- | --------------------------------------------------- |
| **`event`**    | <code><a href="#lottieevent">LottieEvent</a></code> |
| **`callback`** | <code>(ev: any) =&gt; void</code>                   |

--------------------


### once(...)

```typescript
once(event: LottieEvent) => Promise<unknown>
```

| Param       | Type                                                |
| ----------- | --------------------------------------------------- |
| **`event`** | <code><a href="#lottieevent">LottieEvent</a></code> |

**Returns:** <code>Promise&lt;unknown&gt;</code>

--------------------


### Type Aliases


#### LottieEvent

<code>'lottieAnimationStart' | 'lottieAnimationEnd' | 'lottieAnimationCancel' | 'lottieAnimationRepeat'</code>

</docgen-api>
