# Frases motivacionais com Firebase AI Logic

## Implementação

- Modelo: `gemini-3.5-flash-lite`, pelo provedor Gemini Developer API.
- A IA recebe apenas um pedido genérico em português. Os registros do diário e os dados da conta não são enviados.
- Cada chamada busca até 20 frases. O card troca a frase a cada 60 segundos enquanto o diário está visível.
- Ao sair da tela ou colocar o aplicativo em segundo plano, a atualização é suspensa.
- As frases ficam em memória. Quando acabam, o aplicativo busca outro lote.
- Em caso de falha, mantém a frase atual (ou a frase padrão inicial), mostra um aviso e tenta novamente depois de um minuto.
- App Check usa o provedor de depuração no APK debug e Play Integrity no APK release.

## Concluir a configuração no Firebase

1. Abra https://console.firebase.google.com/ e selecione o projeto correspondente ao arquivo `app/google-services.json`.
2. Entre em **AI Services > AI Logic** e clique em **Começar / Get started**.
3. Selecione **Gemini Developer API** e conclua o assistente para habilitar as APIs necessárias. Não basta adicionar as dependências no Gradle.
4. Execute o aplicativo pelo Android Studio e abra o diário.
5. No **Logcat**, selecione o processo `br.com.mcoder.primeiroprojeto` e procure `DebugAppCheckProvider`.
6. Copie o token de depuração exibido no Logcat. Mantenha esse token privado; não o adicione ao código ou ao repositório.
7. No Console Firebase, abra **App Check**, encontre o aplicativo Android, abra o menu de três pontos e selecione **Gerenciar tokens de depuração / Manage debug tokens**.
8. Cadastre o token e volte ao diário. O aplicativo tenta novamente automaticamente após um minuto.
9. Confirme que aparece uma nova frase com o ícone de três brilhos abaixo dela. Após 60 segundos, confirme a troca da frase. O ícone tem descrição para leitores de tela; falhas são exibidas em um aviso temporário.

Se aparecer `ServiceDisabledException`, conclua primeiro o assistente de AI Logic do mesmo projeto usado pelo aplicativo.

Antes de distribuir a versão release, registre o aplicativo no App Check com Play Integrity e configure o certificado SHA-256 da assinatura de distribuição. O token de depuração só atende à versão de desenvolvimento.

## Verificação realizada

Em 30/09/2026:

- `./gradlew.bat testDebugUnitTest assembleDebug`: passou.
- 42 testes executados, sem falhas, incluindo 6 testes novos para validação das frases, intervalo de um minuto, cache e preservação da frase em caso de erro.
- APK instalado e aplicativo aberto no emulador Android.
- Na primeira tentativa, a chamada retornou `ServiceDisabledException`; o card manteve a frase padrão e exibiu o aviso esperado.
- Após habilitar AI Logic e App Check, foi cadastrado o token do emulador com o nome `Emulador Android Studio`. O segredo não foi salvo no projeto.
- O servidor informou que `gemini-2.5-flash-lite` não estava mais disponível para novos usuários. O modelo foi atualizado para `gemini-3.5-flash-lite`, suportado pelo Firebase e com faixa gratuita para texto.
- Os 42 testes locais passaram após a atualização do modelo.
- Dois testes instrumentados passaram no emulador, incluindo uma chamada real ao Gemini com App Check.
- No diário, foi exibida uma frase real da IA e confirmada a troca automática após um minuto, sem aviso de erro.
- Comprovante do cadastro, com token oculto: `verificacao-ia/app-check-emulador.png`.

O teste `MotivationIntegrationTest` só acessa a rede quando recebe o argumento de instrumentação `verifyAi=true`. Para repeti-lo no emulador conectado:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug assembleDebugAndroidTest
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w -e verifyAi true br.com.mcoder.primeiroprojeto.test/androidx.test.runner.AndroidJUnitRunner
```

Referências:

- https://firebase.google.com/docs/ai-logic/get-started?platform=android
- https://firebase.google.com/docs/app-check/android/debug-provider
- https://firebase.google.com/docs/app-check/android/play-integrity-provider
- https://firebase.google.com/docs/ai-logic/models
- https://ai.google.dev/gemini-api/docs/pricing
