# RegionPay

RegionPay is a Minecraft plugin that charges players when they enter a specified WorldGuard region.  
Useful for servers that want to create toll areas, paid zones, restricted access areas, or economic-based regions.

---

## 🔧 Features
- Automatically charges a player when entering a specific region  
- Supports individual fee settings per region  
- Fully configurable via `config.yml` and `region.yml`  
- Lightweight and optimized to minimize performance impact  

---

## 📌 Requirements
- Paper / Spigot **1.21.x**
- Vault (Economy API)
- Economy plugin supported by Vault
- WorldGuard / WorldEdit

---

## 📥 Installation
1. Download `regionpay-1.0.jar`
2. Place it into your `plugins` folder
3. Restart the server
4. Configure `config.yml` and `region.yml` as needed

---

## ⚙️ Configuration
Located in: `/plugins/RegionPay/`

### Example settings  
- Region-based fees  
- Cooldown to prevent repeated charging  
- Global settings (enable/disable messages, defaults, etc.)

---

## 📘 Usage / Commands
| Command               | Description                         |
|----------------------|-------------------------------------|
| `/regionpay reload`  | Reloads configuration files         |

Permissions (if needed) may be added in the future.

---

## 📄 License
This project is distributed under the **MIT License**.  
You are free to use, modify, and distribute it, as long as credit is retained.

---

## 🤝 Contributing
Issues, suggestions, and pull requests are welcome!  
Please submit them through the GitHub repository.

---

## 📝 Notes
This plugin is still in development.  
Future updates may include:
- Region-based discounts
- Support for more event triggers
- API integration extensions

---

