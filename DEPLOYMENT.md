# Deployment Guide - Inventory Manager

This guide will help you deploy your Inventory Manager application to Railway.

## Why Railway?

Railway is recommended for this Spring Boot application because:
- ✅ Native Java/Maven support
- ✅ Built-in PostgreSQL database
- ✅ Automatic builds from Git
- ✅ Free tier available
- ✅ Simple environment variable management
- ✅ HTTPS by default

## Prerequisites

1. A [Railway account](https://railway.app/) (free to sign up)
2. Your code pushed to GitHub
3. PostgreSQL database (Railway provides this)

## Deployment Steps

### 1. Create a Railway Project

1. Go to [Railway.app](https://railway.app/)
2. Click **"Start a New Project"**
3. Select **"Deploy from GitHub repo"**
4. Authorize Railway to access your GitHub account
5. Select your `inventory-manager` repository

### 2. Add PostgreSQL Database

1. In your Railway project, click **"+ New"**
2. Select **"Database"** → **"PostgreSQL"**
3. Railway will create a PostgreSQL database and automatically set the `DATABASE_URL` environment variable

### 3. Configure Environment Variables

Railway automatically provides these variables:
- `DATABASE_URL` - PostgreSQL connection string (auto-configured)
- `PORT` - Port for your application (auto-configured)

**No additional configuration needed!** The application is already set up to use these environment variables.

### 4. Deploy

1. Railway will automatically detect your Maven project
2. It will build using: `mvn clean install`
3. It will start using the Procfile: `java -jar target/inventory-manager-1.0-SNAPSHOT.jar`
4. Wait for the deployment to complete (usually 2-5 minutes)

### 5. Access Your Application

1. In Railway, go to your service settings
2. Click **"Generate Domain"** to get a public URL
3. Your application will be available at: `https://your-app-name.up.railway.app`

## Environment Variables Reference

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `DATABASE_URL` | PostgreSQL connection string | Auto-set by Railway | Yes |
| `PORT` | Application port | 8080 | Auto-set |
| `DATABASE_USER` | Database username | From DATABASE_URL | No |
| `DATABASE_PASSWORD` | Database password | From DATABASE_URL | No |

## Post-Deployment

### Create Your First User

1. Visit your deployed application URL
2. Click on the **"Register"** tab
3. Create your admin account
4. Start managing your inventory!

### Monitor Your Application

Railway provides:
- Real-time logs
- Resource usage metrics
- Deployment history
- Database backups

Access these from your Railway dashboard.

## Troubleshooting

### Application Won't Start

**Check logs in Railway dashboard:**
```bash
# Common issues:
- Database connection failed → Verify DATABASE_URL is set
- Port binding error → Railway sets PORT automatically
- Build failed → Check pom.xml and Java version (11)
```

### Database Connection Issues

1. Ensure PostgreSQL service is running in Railway
2. Check that `DATABASE_URL` environment variable is set
3. Verify the database is in the same Railway project

### Application Shows 404

1. Ensure the build completed successfully
2. Check that the JAR file was created: `target/inventory-manager-1.0-SNAPSHOT.jar`
3. Verify the Procfile is using the correct JAR name

## Alternative Deployment Options

### Render.com

1. Create account at [Render.com](https://render.com/)
2. Click "New +" → "Web Service"
3. Connect GitHub repo
4. Configure:
   - **Build Command:** `mvn clean install`
   - **Start Command:** `java -jar target/inventory-manager-1.0-SNAPSHOT.jar`
5. Add PostgreSQL database from Render dashboard
6. Set `DATABASE_URL` environment variable

### Heroku

1. Install [Heroku CLI](https://devcenter.heroku.com/articles/heroku-cli)
2. Login: `heroku login`
3. Create app: `heroku create your-app-name`
4. Add PostgreSQL: `heroku addons:create heroku-postgresql:hobby-dev`
5. Deploy: `git push heroku master`

## Production Checklist

- [ ] Database backups enabled
- [ ] Environment variables secured
- [ ] HTTPS enabled (automatic on Railway)
- [ ] Created admin user account
- [ ] Tested authentication flow
- [ ] Tested buy/sell operations
- [ ] Verified transaction tracking
- [ ] Monitored application logs

## Security Notes

1. **Never commit** database credentials to Git
2. Use environment variables for all sensitive data
3. Railway provides automatic HTTPS/SSL
4. Change default passwords after first login
5. Regularly backup your database

## Scaling

Railway offers easy scaling:
- Automatic vertical scaling
- Pay-as-you-grow pricing
- Database connection pooling (already configured)

## Support

- Railway Docs: https://docs.railway.app/
- Railway Discord: https://discord.gg/railway
- This project: Check GitHub issues

## Cost Estimate

**Railway Free Tier:**
- $5 credit/month
- Sufficient for small production apps
- ~500 hours of runtime

**Paid Plan:**
- Pay only for what you use
- Approximately $5-10/month for light usage
- Scales based on traffic

---

🎉 **You're ready to deploy!** Follow the steps above and your Inventory Manager will be live in minutes.
