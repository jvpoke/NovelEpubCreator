# Novel Epub Creator

Aplicativo Android para criar arquivos EPUB a partir de sites de novels.

## Funcionalidades

- **Geração de EPUB**: Cria livros digitais formatados a partir de links de novels.
- **Detecção Automática**: Tenta identificar sequencialmente os links dos capítulos.
- **Adição Manual**: Permite adicionar links de capítulos um a um.
- **Tradução Integrada**: Suporte para tradução Online e Offline (usando ML Kit) para Português Brasil.
- **Exportação**: Salva o arquivo EPUB diretamente no armazenamento do dispositivo.

## Como Compilar

1. Abra o projeto no **Android Studio**.
2. Aguarde a sincronização do Gradle.
3. Conecte um dispositivo Android ou use um emulador.
4. Clique em "Run" (ícone de play).

## Bibliotecas Utilizadas

- [Jsoup](https://jsoup.org/) - Para web scraping.
- [Epublib](https://github.com/psiegman/epublib) - Para criação de arquivos EPUB.
- [ML Kit Translation](https://developers.google.com/ml-kit/language/translation) - Para tradução offline.
- [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) - Para processamento assíncrono.

## Requisitos

- Android SDK 24 ou superior.
- Conexão com a internet para download de capítulos e modelos de tradução.
