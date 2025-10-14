# Render.com Deployment Guide

Complete guide to deploy your Inventory Manager to Render's free tier.

## Why Render?

✅ **Free tier** - No credit card required
✅ **PostgreSQL included** - 90 days free, then $7/month
✅ **Auto-deployments** - From GitHub
✅ **Easy setup** - Similar to Railway
✅ **HTTPS included** - Automatic SSL

## Prerequisites

1. [Render account](https://render.com/) (free signup)
2. Your code pushed to GitHub
3. That's it!

---

## Deployment Steps

### Step 1: Push Your Code to GitHub

Make sure your latest code is on GitHub:

```bash
git add .
git commit -m "Add Render deployment configuration"
git push origin master
```

### Step 2: Create Render Account

1. Go to [render.com](https://render.com/)
2. Click **"Get Started"**
3. Sign up with GitHub (recommended)

### Step 3: Deploy from GitHub

#### Option A: Using render.yaml (Recommended - Automatic Setup)

1. In Render Dashboard, click **"New +"** → **"Blueprint"**
2. Connect your GitHub repository
3. Render will detect `render.yaml` and configure everything automatically
4. Click **"Apply"**
5. Wait 3-5 minutes for deployment

**Done!** Render creates both the web service and PostgreSQL database automatically.

#### Option B: Manual Setup

If you prefer manual setup:

**1. Create PostgreSQL Database:**
   - Click **"New +"** → **"PostgreSQL"**
   - Name: `inventory-db`
   - Database: `inventorydb`
   - User: `inventoryuser`
   - Plan: **Free**
   - Click **"Create Database"**

**2. Create Web Service:**
   - Click **"New +"** → **"Web Service"**
   - Connect your GitHub repo
   - Name: `inventory-manager`
   - Region: **Oregon** (or closest to you)
   - Branch: `master`
   - Build Command: `mvn clean install -DskipTests`
   - Start Command: `java -jar target/inventory-manager-1.0-SNAPSHOT.jar`
   - Plan: **Free**

**3. Add Environment Variables:**
   In your web service settings → Environment:

   | Key | Value |
   |-----|-------|
   | `JAVA_VERSION` | `11` |
   | `PGHOST` | Copy from database → "Internal Database URL" → hostname |
   | `PGPORT` | `5432` |
   | `PGDATABASE` | `inventorydb` |
   | `PGUSER` | Copy from database credentials |
   | `PGPASSWORD` | Copy from database credentials |

   **Or use Connection String:**
   Render provides these automatically if services are linked!

**4. Deploy:**
   - Click **"Create Web Service"**
   - Wait for build & deployment

---

## Step 4: Verify Deployment

### Check Logs

In your web service page:
1. Click **"Logs"** tab
2. Look for:
   ```
   ✅ BUILD SUCCESS
   ✅ HikariPool-1 - Start completed
   ✅ Hibernate: create table users
   ✅ Hibernate: create table items
   ✅ Hibernate: create table transactions
   ✅ Tomcat started on port(s): 10000
   ✅ Started InventoryManagerApplication
   ```

### Access Your App

1. Render provides a URL: `https://inventory-manager-XXXX.onrender.com`
2. Click the URL or copy it
3. You should see your login/register page!

### Verify Database Tables

In PostgreSQL service:
1. Click **"Connect"** → **"External Connection"**
2. Use a PostgreSQL client or the web console
3. Verify tables exist: `users`, `items`, `transactions`

---

## Important Free Tier Notes

### Web Service (Free Plan)

⚠️ **Spins down after 15 minutes of inactivity**
- First request after sleep takes ~30 seconds (cold start)
- After that, runs normally
- Perfect for demos and personal projects

💡 **Keep it alive:**
- Use a service like [UptimeRobot](https://uptimerobot.com/) (free) to ping your app every 5 minutes

### PostgreSQL (Free Plan)

⚠️ **Free for 90 days, then $7/month**
- 1GB storage
- 97% uptime SLA
- Automatic backups not included on free tier

💡 **Alternative:** Use free PostgreSQL from:
- [Supabase](https://supabase.com/) - 500MB free forever
- [Neon](https://neon.tech/) - 3GB free forever
- [ElephantSQL](https://www.elephantsql.com/) - 20MB free forever

---

## Configuration Files

Your repository includes:

1. **`render.yaml`** - Automatic deployment configuration
2. **`Procfile`** - Start command (also works on Render)
3. **`system.properties`** - Java version specification
4. **`application.properties`** - Database configuration

All configured and ready to go! ✅

---

## Post-Deployment

### 1. Create Your First User

Visit your app URL and register:
- Click **"Register"** tab
- Create username and password
- Login and start using!

### 2. Custom Domain (Optional)

Render free tier supports custom domains:
1. In service settings → **"Custom Domains"**
2. Add your domain
3. Update DNS records as shown
4. Free SSL included!

---

## Troubleshooting

### Build Fails

**Error:** Maven build timeout
- **Solution:** Check your pom.xml for issues
- Ensure Java 11 is specified in `system.properties`

### Connection Refused

**Error:** Can't connect to database
- **Solution:** Verify environment variables are set
- Check database service is running
- Ensure services are in same region

### Application Crashes on Start

**Error:** App starts then stops
- **Solution:** Check logs for stack traces
- Verify `PORT` environment variable (Render sets this automatically)
- Database connection issues - check credentials

### Tables Not Created

**Error:** Database empty
- **Solution:** Check logs for Hibernate DDL statements
- Verify `spring.jpa.hibernate.ddl-auto=update` in application.properties
- Database user must have CREATE TABLE permissions

---

## Monitoring & Maintenance

### View Logs
```bash
# In Render dashboard
Click service → Logs tab
```

### Redeploy
```bash
# Automatic: Push to GitHub
git push origin master

# Manual: In Render dashboard
Click service → Manual Deploy → Deploy latest commit
```

### Database Backups
Free tier doesn't include automatic backups. Consider:
- Upgrading to paid tier ($7/month)
- Manual exports via pg_dump
- Using an external backup service

---

## Scaling & Costs

### Free Tier Limits
- ✅ 750 hours/month web service
- ✅ 100GB bandwidth
- ✅ Enough for personal projects

### Paid Upgrades
If you need more:
- **Starter Plan:** $7/month - No sleep, 1GB RAM
- **Standard Plan:** $25/month - 4GB RAM, better performance
- **PostgreSQL:** $7/month after free 90 days

---

## Support

- **Render Docs:** https://render.com/docs
- **Community:** https://community.render.com/
- **Status:** https://status.render.com/

---

## Comparison: Render vs Railway

| Feature | Render Free | Railway Free |
|---------|-------------|--------------|
| Web Service | ✅ Free (sleeps) | ✅ $5 credit/month |
| PostgreSQL | ✅ 90 days free | ✅ Included |
| Cold Starts | Yes (~30s) | No |
| Build Time | Not counted | Counted |
| Credit Card | Not required | Not required |
| **Best For** | Demos, prototypes | Active development |

---

🎉 **You're all set!** Your Inventory Manager is ready to deploy on Render's free tier.
