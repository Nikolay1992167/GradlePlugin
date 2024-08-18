<h1 align="center">Gradle Plugin</h1>

<details>
 <summary><strong>
  Техническое задание
</strong></summary>

#### ЗАДАНИЕ:

#### Тестовое задание 3:

* Разработка Gradle Plugin
* 1 Плагин должен добавить задачу в проект
* 2 В зависимости от того, в какой ветке задача была запущена:
*    1 Определить последнюю опубликованную версию  (последний git tag)
*    2 Определить версию текущего билда исходя из следующей логики
*        1. dev/qa - инкремент минорной версии
*        2. stage - добавить постфикс -rc
*        3. master - инкремент мажорной версии
*        4. Из любой другой ветки - постфикс -SNAPSHOT
*    3 Присвоить соответствующий git tag
*    4 Опубликовать его в origin
* Если текущему состоянию проекта уже присвоен git tag, новый присваиваться не должен
* Если в рабочей директории есть незакоммиченные изменения, вывести в лог номер сборки с постфиксом .uncommitted, git tag при этом не создавать

* Ожидаемый результат
* - Если тегов нет
*  При запуске из ветки dev будет опубликован tag v0.1				
*  После слияния изменений из dev в qa и запуске из qa v0.2				
*  После слияния изменений из qa в stage и запуске из stage v0.3-rc				
*  После слияния изменений из stage в master и запуске из master v1.0

* - Если тег v1.0 есть
*  При запуске из ветки dev будет опубликован tag v1.1				
*  После слияния изменений из dev в qa и запуске из qa v1.2				
*  После слияния изменений из qa в stage и запуске из stage v1.3-rc				
*  После слияния изменений из stage в master и запуске из master v2.0
</details>

<details>
 <summary><strong>
  Запуск проекта
</strong></summary>

* Необходимо перейти в ветку feature/gittagplugin.
* Запустить задачу из меню IDEA в директории buildSrc->Tasks->publishing->publishToMavenLocal или
с помощью команды `./gradlew -p buildSrc publishToMavenLocal`
* Затем можно проверить, что в директории .m2/repository/by/mnp создан git-tag-plugin.
* Чтобы использовать данный плагин, необходимо в setting.gradle добавить:
````groovy
pluginManagement {
    it.repositories {
        it.mavenLocal()
        it.gradlePluginPortal()
    }
}
````
* А в build.gradle в корне проекта необходимо добавить:
````groovy
plugins {
    id 'git-tag-plugin' version '1.0.0'
}

pushTag {
    checkUncommitted = true
}
````
* флаг checkUncommitted необходим чтобы отслеживать незакоммиченные изменения. Также необходимо добавить 
* зависимость выполнения задач `build.finalizedBy pushTag`.

</details>

<details>
 <summary><strong>
  Применение плагина
</strong></summary>

* После подключения плагина появиться задача pushTag в папке git
* Если Git не установлен локально либо как у меня было отсутствует путь в системных переменных вы получите:
````text
Git not found on current device
````
* Затем можно создать от ветки master ветки dev, qa, stage, а для тестирования сторонней ветки можно использовать
* feature/gittagplugin.
* Можем проверить правильность работы плагина в ветке мастер. Если установлен флаг checkUncommitted = true и 
* не установлен tag, получим результат 
````text
Detected uncommitted changes in repository without tags
````
* Затем при выполнении задачи в ветке dev получим результат:
````text
The current commit is assigned tag v0.1
````
* Затем выполняя мержинг согласно условиям задания, мы будем получать соответствующий результат с инкрементом
* минорной версии.
* Но в данном случае если мержить при помощи возможностей IDEA необходимо создавать результирующий коммит. 
* Это я обошёл используя команду `git merge --no-ff dev`. При использовании флага --no-ff создаётся результирующий 
* коммит при слиянии.
* После слияния в ветку master, tag будет:
```text
The current commit is assigned tag v1.0
```
* И если ещё раз пройти от ветки master, то получим результат с учётом существующей версии тега.
* При попытке присвоить тег коммиту, который имеет тэг, получим сообщение:

````text
The current state of the project is already tagged v0.1 by git
````
</details>