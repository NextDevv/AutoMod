# AutoMod - Coming Soon

AutoMod is a Minecraft Spigot plugin designed to enhance in-game chat moderation by using AI, specifically the Perspective API, to censor and block harmful messages. The plugin also blocks links, domains, and IP addresses (both IPv4 and IPv6). Additionally, AutoMod supports multi-instance servers through Redis communication and utilizes caches to speed up the moderation process, with configurable cache expiration.

# Docs

[Coming soon for AutoMod...](https://docs.nextdevv.com/)

## Features

- **AI-Powered Moderation:** Uses Perspective API to detect and censor harmful messages in real-time.
- **Link and IP Blocking:** Blocks links, domains, IPv4, and IPv6 addresses to prevent spam and malicious content.
- **Multi-Instance Support:** Utilizes Redis to communicate between multiple server instances for synchronized moderation.
- **Configurable Caching:** Implements caches to improve performance with customizable expiration settings.

## Installation

1. Download the latest release of AutoMod from the [releases page](https://github.com/nextdevv/automod/releases).
2. Place the `AutoMod.jar` file into your server's `plugins` directory.
3. Restart your Minecraft server to load the plugin.

## Configuration

After the initial run, a configuration file will be generated in the `plugins/AutoMod` directory. The main configuration options include:

- **Perspective API Key:** Obtain an API key from the [Perspective API website](https://perspectiveapi.com/) and add it to the configuration file.
- **Redis Configuration:** Set up Redis connection details to enable multi-instance communication.
- **Cache Settings:** Configure the cache expiration time to balance performance and resource usage.

## Commands

- `/automod reload` - Reloads the plugin configuration.

## Permissions

- `automod.admin` - Access to all AutoMod commands and features.

## Usage

AutoMod automatically moderates chat messages, blocking harmful content, links, and IP addresses based on the configuration. There is no need for additional commands to start the moderation process.

## Contributing

1. Fork the repository on GitHub.
2. Create a new branch (`git checkout -b feature-branch`).
3. Make your changes and commit them (`git commit -am 'Add new feature'`).
4. Push the branch (`git push origin feature-branch`).
5. Create a new Pull Request.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.

## Support

For support, please open an issue on the [GitHub repository](https://github.com/nextdevv/automod/issues) or contact us via email at support@unilix.it.

---

Thank you for using AutoMod! We hope it helps keep your Minecraft server chat safe and enjoyable for everyone.
