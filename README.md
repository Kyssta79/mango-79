# 🥭 MangoParty - Advanced Minecraft Party System

*Automatically synced with your [v0.dev](https://v0.dev) deployments*

[![Deployed on Vercel](https://img.shields.io/badge/Deployed%20on-Vercel-black?style=for-the-badge&logo=vercel)](https://vercel.com/moiz7865s-projects/v0-minecraft-party-system)
[![Built with v0](https://img.shields.io/badge/Built%20with-v0.dev-black?style=for-the-badge)](https://v0.dev/chat/projects/J9y9uOvca6a)
[![Version](https://img.shields.io/badge/version-1.0.0-brightgreen.svg)](https://github.com/your-repo/mangoparty)
[![Minecraft](https://img.shields.io/badge/minecraft-1.21.4-blue.svg)](https://minecraft.net)
[![License](https://img.shields.io/badge/license-MIT-yellow.svg)](LICENSE)

## Overview

This repository will stay in sync with your deployed chats on [v0.dev](https://v0.dev).
Any changes you make to your deployed app will be automatically pushed to this repository from [v0.dev](https://v0.dev).

**MangoParty** is a comprehensive party system plugin for Minecraft servers that provides advanced PvP functionality, including party management, arena-based matches, queue systems, and party vs party duels.

## Deployment

Your project is live at:

**[https://vercel.com/moiz7865s-projects/v0-minecraft-party-system](https://vercel.com/moiz7865s-projects/v0-minecraft-party-system)**

## Build your app

Continue building your app on:

**[https://v0.dev/chat/projects/J9y9uOvca6a](https://v0.dev/chat/projects/J9y9uOvca6a)**

## How It Works

1. Create and modify your project using [v0.dev](https://v0.dev)
2. Deploy your chats from the v0 interface
3. Changes are automatically pushed to this repository
4. Vercel deploys the latest version from this repository

## 🌟 Features

### 🎉 **Party System**
- **Create & Manage Parties**: Form parties with friends using intuitive commands
- **Party Leadership**: Transfer leadership, invite/kick members, and manage party settings
- **Smart Invitations**: Clickable invite system with 60-second expiration
- **Party Chat**: Dedicated communication channel for party members
- **Match Integration**: Seamless integration with all match types

### ⚔️ **Match Types**

#### **1. Party Split Matches**
- Divide your party into two balanced teams
- Fight against your friends in structured team battles
- Automatic team assignment with spawn point allocation

#### **2. Party FFA (Free For All)**
- Last player standing wins
- All party members fight each other
- Perfect for determining the ultimate champion

#### **3. Party vs Party Duels**
- Challenge other parties to epic team battles
- Leader-to-leader challenge system with clickable accept/decline
- 60-second challenge expiration timer
- Automatic team assignment (Party 1 vs Party 2)

#### **4. Queue System (1v1, 2v2, 3v3)**
- **Ranked Matchmaking**: Join queues for competitive matches
- **Kit-Based Queuing**: Queue with specific kits for balanced gameplay
- **Auto-Matching**: Automatic match creation when enough players queue
- **Live Queue Counts**: See how many players are queued for each kit
- **Smart Team Balancing**: Random team assignment for fair matches

### 🏟️ **Arena Management**
- **Visual Arena Editor**: GUI-based arena setup with click-to-set locations
- **Schematic System**: Automatic arena regeneration using WorldEdit schematics
- **Multi-Arena Support**: Multiple arenas with automatic availability checking
- **Arena Bounds**: Automatic teleportation when players leave arena boundaries
- **Arena Reservation**: Smart arena allocation to prevent conflicts

### 🎒 **Kit System**
- **Custom Kits**: Create kits from player inventories
- **Kit Rules Engine**: Advanced rule system for each kit
  - Natural health regeneration toggle
  - Block breaking/placing permissions
  - Damage multipliers (e.g., +33% damage)
  - Instant TNT explosion
- **Kit Icons**: Custom item icons for GUI representation
- **Kit Editor**: Visual kit rule configuration through GUIs

### 📊 **Match Features**
- **Live Scoreboards**: Real-time match statistics using FastBoard
- **Kill/Death Tracking**: Comprehensive player statistics
- **Spectator System**: Dead players become invisible spectators with flight
- **Match States**: Proper state management (Preparing → Countdown → Active → Ending)
- **Team Management**: Advanced team assignment and tracking
- **Match Duration**: Automatic match timing and duration tracking

### 🎮 **User Interface**
- **Intuitive GUIs**: Beautiful, easy-to-use graphical interfaces
- **Clickable Messages**: Interactive chat messages with hover effects
- **Visual Feedback**: Clear status indicators and progress displays
- **Responsive Design**: Adaptive GUIs that work with different screen sizes

## 📋 Commands

### **Party Commands**
\`\`\`
/party create                    - Create a new party
/party invite <player>           - Invite a player to your party
/party join <leader>             - Join a party (if invited)
/party transfer <player>         - Transfer party leadership
/party leave                     - Leave your current party
/party disband                   - Disband your party (leader only)
/party match                     - Open match type selection GUI
/party challenge <leader>        - Challenge another party to a duel
/party acceptduel <challenger>   - Accept a party duel challenge
/party declineduel <challenger>  - Decline a party duel challenge
/party info                      - View party information
\`\`\`

### **Queue Commands**
\`\`\`
/1v1queue                        - Join 1v1 queue (opens kit selection)
/2v2queue                        - Join 2v2 queue (opens kit selection)
/3v3queue                        - Join 3v3 queue (opens kit selection)
/leavequeue                      - Leave current queue
\`\`\`

### **Spectator Commands**
\`\`\`
/spectate <player>               - Spectate a specific player in your match
\`\`\`

### **Admin Commands**
\`\`\`
/mango arena editor              - Open arena management GUI
/mango arena create <name>       - Create a new arena
/mango arena corner1 <name>      - Set arena corner 1
/mango arena corner2 <name>      - Set arena corner 2
/mango arena center <name>       - Set arena center
/mango arena spawn1 <name>       - Set arena spawn point 1
/mango arena spawn2 <name>       - Set arena spawn point 2
/mango arena save <name>         - Save arena schematic
/mango arena list                - List all arenas
/mango arena delete <name>       - Delete an arena

/mango kit editor                - Open kit management GUI
/mango create kit <name>         - Create kit from current inventory

/mango addkitgui <kit> <mode> [slot] - Add kit to GUI configuration
/mango setspawn                  - Set server spawn location
\`\`\`

## 🔧 Installation

### **Requirements**
- **Minecraft Server**: 1.21.4 (Spigot/Paper recommended)
- **Java**: 17 or higher
- **Dependencies**:
  - WorldEdit (for arena schematics)
  - FastAsyncWorldEdit (optional, for better performance)

### **Installation Steps**

1. **Download the Plugin**
   \`\`\`bash
   # Download MangoParty-1.0.0.jar from releases
   wget https://github.com/your-repo/mangoparty/releases/download/v1.0.0/MangoParty-1.0.0.jar
   \`\`\`

2. **Install Dependencies**
   - Download and install [WorldEdit](https://dev.bukkit.org/projects/worldedit)
   - Optionally install [FastAsyncWorldEdit](https://www.spigotmc.org/resources/fastasyncworldedit.13932/)

3. **Server Setup**
   \`\`\`bash
   # Place the plugin in your plugins folder
   cp MangoParty-1.0.0.jar /path/to/server/plugins/
   
   # Start your server
   java -jar server.jar
   \`\`\`

4. **Initial Configuration**
   \`\`\`bash
   # Set server spawn point
   /mango setspawn
   
   # Create your first arena
   /mango arena create arena1
   /mango arena corner1 arena1    # Set at one corner
   /mango arena corner2 arena1    # Set at opposite corner
   /mango arena center arena1     # Set center point
   /mango arena spawn1 arena1     # Set team 1 spawn
   /mango arena spawn2 arena1     # Set team 2 spawn
   /mango arena save arena1       # Save the schematic
   \`\`\`

## ⚙️ Configuration

### **Main Configuration** (`config.yml`)
\`\`\`yaml
# Party Settings
party:
  max-size: 8                    # Maximum party size
  invite-timeout: 60             # Invite timeout in seconds
  auto-disband: true             # Auto-disband empty parties

# Match Settings
match:
  countdown-duration: 5          # Countdown before match starts
  min-players: 2                 # Minimum players to start
  max-duration: 30               # Max match duration (minutes)

# Arena Settings
arena:
  auto-regenerate: true          # Auto-regenerate arenas after matches
  regeneration-delay: 5          # Delay before regeneration

# Scoreboard Configuration
scoreboard:
  title: "&#FFD700&lMangoParty"
  lines:
    - ""
    - "&#FF6B6BArena: &#FFFFFF{arena}"
    - "&#4ECDC4Kit: &#FFFFFF{kit}"
    - "&#45B7D1Mode: &#FFFFFF{match_type}"
    - ""
    - "&#FFE66DStatus: {status}"
    - "&#4ECDC4Players: &#FFFFFF{players_alive}/{players_total}"
    - ""
    - "&#FFEAA7Stats:"
    - "  &#FF7675Kills: &#FFFFFF{kills}"
    - "  &#74B9FFDeaths: &#FFFFFF{deaths}"
    - ""
    - "&#A29BFETime: &#FFFFFF{time}"
\`\`\`

### **GUI Configuration**
The plugin automatically generates GUI configuration files:

- `gui/split.yml` - Party split match kits
- `gui/ffa.yml` - Party FFA match kits
- `gui/1v1kits.yml` - 1v1 queue kits
- `gui/2v2kits.yml` - 2v2 queue kits
- `gui/3v3kits.yml` - 3v3 queue kits

### **Arena Configuration** (`arenas.yml`)
\`\`\`yaml
arenas:
  arena1:
    world: "world"
    corner1:
      x: 100.0
      y: 64.0
      z: 100.0
    corner2:
      x: 150.0
      y: 80.0
      z: 150.0
    center:
      x: 125.0
      y: 65.0
      z: 125.0
    spawn1:
      x: 110.0
      y: 65.0
      z: 125.0
    spawn2:
      x: 140.0
      y: 65.0
      z: 125.0
\`\`\`

## 🎯 Usage Guide

### **Setting Up Your First Arena**

1. **Create the Arena Structure**
   - Build your arena in the world
   - Make sure it has clear boundaries
   - Include spawn points for both teams

2. **Configure the Arena**
   \`\`\`bash
   /mango arena create myarena
   # Stand at one corner and run:
   /mango arena corner1 myarena
   # Stand at the opposite corner and run:
   /mango arena corner2 myarena
   # Stand at the center and run:
   /mango arena center myarena
   # Set spawn points for teams
   /mango arena spawn1 myarena
   /mango arena spawn2 myarena
   # Save the schematic
   /mango arena save myarena
   \`\`\`

### **Creating Custom Kits**

1. **Prepare Your Kit**
   - Equip the items you want in the kit
   - Include armor, weapons, and consumables

2. **Create the Kit**
   \`\`\`bash
   /mango create kit warrior
   \`\`\`

3. **Configure Kit Rules**
   \`\`\`bash
   /mango kit editor
   # Click on your kit to configure rules
   \`\`\`

4. **Add Kit to GUIs**
   \`\`\`bash
   # Add to party split GUI
   /mango addkitgui warrior split 10
   
   # Add to 1v1 queue GUI
   /mango addkitgui warrior 1v1 11
   \`\`\`

### **Starting Matches**

#### **Party Matches**
1. Create a party: `/party create`
2. Invite friends: `/party invite <player>`
3. Start a match: `/party match`
4. Select match type and kit

#### **Queue Matches**
1. Join a queue: `/1v1queue`, `/2v2queue`, or `/3v3queue`
2. Select your kit
3. Wait for matchmaking
4. Get automatically matched with other players

#### **Party vs Party Duels**
1. Create parties (both leaders)
2. Challenge: `/party challenge <other_leader>`
3. Or use GUI: `/party match` → Party vs Party
4. Accept/decline the challenge
5. Fight in epic team battles!

## 🛠️ Development

### **Building from Source**
\`\`\`bash
# Clone the repository
git clone https://github.com/your-repo/mangoparty.git
cd mangoparty

# Build with Maven
mvn clean compile package

# The compiled JAR will be in target/
\`\`\`

### **Project Structure**
\`\`\`
src/main/java/me/moiz/mangoparty/
├── commands/           # Command handlers
├── config/            # Configuration management
├── gui/               # GUI systems
├── listeners/         # Event listeners
├── managers/          # Core managers
├── models/            # Data models
└── utils/             # Utility classes
\`\`\`

### **Key Components**

- **PartyManager**: Handles party creation, management, and member operations
- **MatchManager**: Controls match lifecycle, team assignment, and game states
- **ArenaManager**: Manages arena loading, schematic operations, and availability
- **QueueManager**: Handles queue operations and automatic matchmaking
- **GuiManager**: Manages all GUI interactions and displays
- **KitManager**: Handles kit creation, loading, and application

## 🔌 API Integration

### **Events**
The plugin fires custom events that other plugins can listen to:

\`\`\`java
// Match events
@EventHandler
public void onMatchStart(MatchStartEvent event) {
    Match match = event.getMatch();
    // Handle match start
}

@EventHandler
public void onMatchEnd(MatchEndEvent event) {
    Match match = event.getMatch();
    UUID winner = event.getWinner();
    // Handle match end
}

// Party events
@EventHandler
public void onPartyCreate(PartyCreateEvent event) {
    Party party = event.getParty();
    Player leader = event.getLeader();
    // Handle party creation
}
\`\`\`

### **Hooks**
\`\`\`java
// Get the MangoParty instance
MangoParty plugin = (MangoParty) Bukkit.getPluginManager().getPlugin("MangoParty");

// Check if player is in a party
boolean inParty = plugin.getPartyManager().hasParty(player);

// Get player's current match
Match match = plugin.getMatchManager().getPlayerMatch(player);

// Check if player is in queue
boolean inQueue = plugin.getQueueManager().isInQueue(player);
\`\`\`

## 🎨 Customization

### **Scoreboard Placeholders**
Available placeholders for scoreboard customization:

- `{arena}` - Arena name
- `{kit}` - Kit display name
- `{match_type}` - Match type (SPLIT, FFA, etc.)
- `{status}` - Player status (Alive, Spectating)
- `{players_alive}` - Number of alive players
- `{players_total}` - Total players in match
- `{spectators}` - Number of spectators
- `{kills}` - Player's kill count
- `{deaths}` - Player's death count
- `{time}` - Match duration
- `{your_team_alive}` - Your team's alive count
- `{opponent_team_alive}` - Opponent team's alive count

### **Color Codes**
The plugin supports both legacy (`&c`) and hex color codes (`&#FF0000`):

\`\`\`yaml
title: "&#FFD700&lMangoParty"  # Gold with bold
subtitle: "&#FF6B6B&oWelcome!" # Red with italic
\`\`\`

## 🐛 Troubleshooting

### **Common Issues**

#### **"No available arenas" Error**
- **Cause**: No arenas configured or all arenas in use
- **Solution**: Create more arenas or wait for current matches to end

#### **Schematic Not Loading**
- **Cause**: WorldEdit not installed or schematic file corrupted
- **Solution**: Install WorldEdit and regenerate arena schematic

#### **Queue Not Working**
- **Cause**: No kits configured for queue mode
- **Solution**: Add kits to queue GUIs using `/mango addkitgui`

#### **Players Not Teleporting**
- **Cause**: Arena spawn points not set correctly
- **Solution**: Reconfigure arena spawn points

### **Debug Commands**
\`\`\`bash
# Check arena status
/mango arena list

# Verify kit configuration
/mango kit editor

# Check plugin version
/version MangoParty
\`\`\`

## 📈 Performance

### **Optimization Tips**

1. **Arena Management**
   - Use smaller schematics for faster regeneration
   - Limit the number of concurrent matches
   - Use FastAsyncWorldEdit for better performance

2. **Database Optimization**
   - Regular cleanup of old match data
   - Optimize scoreboard update frequency

3. **Memory Management**
   - Monitor plugin memory usage
   - Regular server restarts for long-running servers

## 🤝 Contributing

We welcome contributions! Please follow these guidelines:

1. **Fork the Repository**
2. **Create a Feature Branch**
   \`\`\`bash
   git checkout -b feature/amazing-feature
   \`\`\`
3. **Make Your Changes**
4. **Test Thoroughly**
5. **Submit a Pull Request**

### **Code Style**
- Use 4 spaces for indentation
- Follow Java naming conventions
- Add JavaDoc comments for public methods
- Include unit tests for new features

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **WorldEdit Team** - For the amazing schematic system
- **FastBoard** - For efficient scoreboard management
- **Spigot/Paper** - For the excellent server platform
- **Community** - For feedback and feature suggestions

## 📞 Support

- **Discord**: [Join our Discord](https://discord.gg/your-server)
- **Issues**: [GitHub Issues](https://github.com/your-repo/mangoparty/issues)
- **Wiki**: [Plugin Wiki](https://github.com/your-repo/mangoparty/wiki)
- **Email**: support@mangoparty.com

---

**Made with ❤️ by the MangoParty Team**

*Transform your Minecraft server into an epic PvP battleground!*
