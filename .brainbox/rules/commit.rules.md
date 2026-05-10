In general, Semantic Commits are formatted messages with human and machine-readable meanings, which follow particular conventions.
This means, that this is merely a guideline for commit messages, so that:
1.	The commit messages are semantic - because these are categorized into meaningful types, indicating the essence of the commit.
2.	The commit messages are conventional - because these are formatted by a consistent structure of well-known types, both for developers and tools.

Further to that, semantic commits might come in handy when we typically need to:
1.	Allow maintainers and contributors to easily browse the project history and understand the essence of changes, while ignoring unimportant changes by commit message type.
2.	Enforce restricted commit structure, thereby encouraging smaller commits with a specific purpose.
3.	Commit the message subject directly, without messing with the wording.
4.	Bump the package version automatically, based on commit message types.
5.	Generate CHANGELOGs and release notes automatically.

So, in essence, semantic commits are dedicated to achieving better readability, velocity, and automation.

##Commit Message Format
A semantic commit message consists of three parts - `header`, `body`, and `footer`.

###The Header
The header is a mandatory line that simply describes the purpose of the change (up to 72 characters).
It is often referred to as the “Summary” and it should consist of three parts:
1.	Type - a short prefix that represents the kind of change (See [Commit Types] below)
2.	Subject - represents a concise description of the actual change.

Practically, in terms of Git, it is merely the first line of the commit message.

`Example`
```
🐞fix: disable confirm button until tick box ticked
```

The message is separated by ‘: ’.
1.	The left partition is the Type which we hypothetically name “prefix”.
2.	The right partition obviously constitutes the Subject of the commit.
a.	The subject should be kept short and to the point.
b.	Simple future tense wording should be used, for example: “add null check” instead of past tense “added a null check”.

Simply put, the above example header meaning is - “This change fixes a defect by disabling the confirm button until a tick box has been ticked”.

###The Body
The body is an optional element that introduces the motivation behind the change or just a slightly more detailed description of the change.

`Example`
```
🐞fix: disable confirm button until approved is ticked
It was flagged that the confirm button was being clicked prior to a user ticking the approved checkbox. This caused issues and needed to be resolved.
```

###The Footer
The footer is a mandatory place to link related work items.  It can also, optionally, be used to mention consequences which stem from the change - such as announcing a breaking change, mentioning contributors and so on.

`Example`
```
🐞fix: disable confirm button until approved is ticked
It was flagged that the confirm button was being clicked prior to a user ticking the approved checkbox. This caused issues and needed to be resolved.
```

In this example, we plainly add a reference to the relevant defect report and nothing else.

##Pull Request Message Format
A semantic Pull Request message consists of three parts - `header`, `body`, and `footer`.

##The Header
The header is a mandatory line that simply describes the purpose of the pull request (up to 72 characters).
It is often referred to as the “Summary” and it should consist of three parts:
1.	Type - a short prefix that represents the kind of change (See [Commit Types] below)
3.	Subject - represents a concise description of the actual change.

Practically, in terms of Git, it is merely the first line of the commit message.

`Example`
```
✨feature: Location Groups admin
```

The message is separated by ‘: ’.
4.	The left partition is the Type which we hypothetically name “prefix”.
5.	The right partition obviously constitutes the Subject of the commit.
- The subject should be kept short and to the point.
- Simple future tense wording should be used, for example: “create Location Group admin” instead of past tense “created Location Group admin”.

Simply put, the above example header meaning is - “This change adds a new feature called Location Groups to the system administration”.


###The Body
The body is an optional element that introduces the motivation behind the change, a slightly more detailed description of the change, or an opportunity for you to request the pull request approver to review a specific piece of code that you may not be 100% happy with.

`Example`
```
✨feature: Location Groups admin
Created Location Groups administration.
Please review my array assignment code in file `LocationGroupEdit.vue` line 198. It works but I feel it could be improved.
```

###The Footer
The footer is a mandatory place to link related work items.  I can also, optionally, be used to mention consequences which stem from the change - such as announcing a breaking change, mentioning contributors and so on.

`Example`
```
✨feature: Location Groups admin
Created Location Groups administration.
Please review my array assignment code in file `LocationGroupEdit.vue` line 198. It works but I feel it could be improved.
```

In this example, we add a reference to the relevant task and denote that it is ‘ready’.

`Example`
```
✨feature: Session Actions
Added additional actions to the session screens.
```

##Commit Types
On top of defining the commit message format, we also define a list of useful prefixes that cover various types of changes.
Attaching Emojis to the commit message helps improve the readability even more, so that we can identify them quickly and easily while browsing the commit history.
Below are the Types that you should be using:

| Prefix | Description |
|--|--|
| ✨feature | Adding additional functionality |
| 🐞fix | Defect fixes |
| ⛏minor | Small changes to existing code |
| 🎨style | Changes that only affect the styling of the software |
| ♻️refactor | Changes to code that do not affect the way the code works |
| 🚧wip | Code in an unfinished state |
| 📝docs | Documentation updates |
| ✅test | Unit tests |
| 👷build | Anything related to the build system and package dependencies. |
| 🔁merge | Manual branch merges |

Only to be used during the Pull Request approval process:
| Prefix | Description |
|--|--|
| 🧲PR | Pull Requests |
| 🍒cherry-pick | Pull Request cherry picking from one branch to another |
| ⚓promote | Upgrade one branch from another |
| 🔥hotfix | Hotfix release |
