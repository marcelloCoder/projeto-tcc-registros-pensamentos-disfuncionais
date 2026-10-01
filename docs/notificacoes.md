# Notificações do Mind Check

## Comportamento

- **Inspiração diária:** uma mensagem por dia, agendada para as 9h no fuso do aparelho, com primeiro nome, frase motivadora e emoji.
- **Incentivo após um registro:** uma mensagem de felicitação após salvar um registro novo com sucesso. Editar um registro não gera felicitação.
- As frases são locais e variadas, disponíveis sem internet. O conteúdo dos registros não é incluído nas notificações.
- Tocar na notificação abre o diário. Se a sessão tiver terminado, abre a tela de entrada.
- Ao sair da conta, o aplicativo cancela o agendamento e remove suas notificações.
- No Android 13 ou posterior, é solicitada a permissão de notificações após entrar na conta. Se houver recusa, o aplicativo continua funcionando normalmente.
- Em **Configurações > Configurar notificações**, o usuário pode ativar ou desativar cada categoria nas configurações do Android.
- A prévia da tela bloqueada usa uma mensagem genérica, respeitando também as preferências de privacidade do Android.

## Agendamento

Usa `AlarmManager.setAndAllowWhileIdle`, sem dependências novas e sem permissão de alarme exato. O horário pode atrasar por decisões de economia de bateria do Android. O primeiro agendamento feito após as 9h começa no dia seguinte.

O agendamento é restaurado após reiniciar o aparelho, atualizar o aplicativo ou alterar o relógio/fuso. A data da última entrega é persistida para evitar mensagens diárias duplicadas. Um alarme atrasado de hoje não é descartado ao reabrir o aplicativo.

Remover o aplicativo da lista de recentes não cancela o alarme. Usar **Forçar parada** nas configurações do Android interrompe os agendamentos até abrir o aplicativo novamente. O aparelho precisa estar ligado e as notificações permitidas para a entrega.

## Verificação

Os testes locais cobrem horário, mudança de horário de verão, limite diário, relógio voltando no tempo, personalização, seleção de frases e diferença entre criação e edição.

O teste instrumentado de notificações é opcional: publica mensagens de teste com o nome Ana, verifica os canais, a privacidade e o cancelamento, sem criar registros no diário. Ao terminar, remove suas mensagens e restaura o agendamento da conta atual.

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug assembleDebugAndroidTest
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w -e class br.com.mcoder.primeiroprojeto.NotificationIntegrationTest -e verifyNotifications true br.com.mcoder.primeiroprojeto.test/androidx.test.runner.AndroidJUnitRunner
```

Esse teste habilita a permissão de notificações no emulador de teste. Os testes normais não publicam notificações automaticamente.

Referências oficiais:

- https://developer.android.com/develop/background-work/services/alarms
- https://developer.android.com/develop/ui/views/notifications/notification-permission
- https://developer.android.com/develop/ui/views/notifications/build-notification
