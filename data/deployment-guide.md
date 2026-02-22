# The Ultimate HisabKitab Cloud Deployment Guide (100% Free Tier)

This guide takes your complete full-stack HisabKitab application from your local Windows machine and puts it live on the internet, 100% for free. 

Because HisabKitab uses modern architecture, it is broken into three layers. You must deploy them in this exact order:
1. **The Database** (A remote MySQL server to hold data)
2. **The Backend API** (A Java server to process calculations)
3. **The Frontend GUI** (A React webpage for users to click)

---

## Stage 1: Local Preparation (Already Completed)
Before your code can be put on the internet, it must be stripped of local assumptions (like `localhost:8080`) and sensitive secrets (like database passwords). 

**We have already architected this for you:**
1. **Frontend `.env` injection:** Your React `axios.js` is no longer hardcoded. It listens for `process.env.VITE_API_URL` which the cloud server will provide dynamically.
2. **Backend Configuration Extraction:** Your Spring Boot `application.properties` no longer hardcodes your root password. It uses `${DB_PASSWORD}` environment variables.
3. **Secure Version Control:** The application has been committed to Git using a strict `.gitignore` that prevents your private passwords from leaking online.

You have already safely run `git push origin main` and your code is securely on GitHub. Proceed to Stage 2.

---

## Stage 2: Database Hosting (Aiven.io)
Your laptop's MySQL is off when you sleep. You need a database that never sleeps.

1. Go to **[Aiven.io](https://aiven.io/)** and create a free account.
2. Click **Create Service**.
3. Select **MySQL** as the service type.
4. Select the **Free Plan** (Note: Depending on availability, you can also use **TiDB Serverless** or **PlanetScale** for free MySQL).
5. Select a region close to your target users (e.g., `aws-ap-south-1` for Mumbai).
6. Click **Create Service**.

**CRITICAL:** Wait 2 minutes for it to build. Once the status shows "Running", locate your **Connection Details**. You specifically need:
* **Host URL:** (e.g., `mysql-123.aivencloud.com:12345/defaultdb`)
* **Username:** (e.g., `avnadmin`)
* **Password:** (Copy this securely)

---

## Stage 3: Backend Hosting (Render.com)
Render will download your Java code from GitHub, package it using Maven, and host the REST API on a public domain.

1. Log into **[Render.com](https://render.com/)** and link your GitHub account.
2. Click the shiny **New** button and select **Web Service**.
3. Select your `HisabKitab---Advanced-Finance-Loan-Management` GitHub repository.
4. **Fill out the Build Settings exactly like this:**
   * **Name:** `hisab-kitab-api`
   * **Root Directory:** `backend` *(<- This is incredibly important)*
   * **Environment:** `Java`
   * **Build Command:** `./mvnw clean package -DskipTests` *(This downloads packages and builds the JAR file)*
   * **Start Command:** `java -jar target/HisabKitab-0.0.1-SNAPSHOT.jar` *(This actually boots Spring)*
   * **Instance Type:** Free

5. **Set Environment Variables:**
   Scroll down before clicking Create. You must manually feed the locked variables to your app. Click "Add Environment Variable":
   * `DB_URL` = `jdbc:mysql://[YOUR_AIVEN_HOST_URL]?useSSL=true`
   * `DB_USERNAME` = `[YOUR_AIVEN_USERNAME]`
   * `DB_PASSWORD` = `[YOUR_AIVEN_PASSWORD]`
   * `JWT_SECRET` = `(Type a long random string of letters and numbers here, at least 32 characters)`
   * `JWT_EXPIRATION` = `86400000` *(This equals 24 hours in milliseconds)*
   * `ALLOWED_ORIGINS` = `[Leave Blank For Now, we will add it later]`

6. Click **Create Web Service**. 

**Wait 3-5 minutes.** Render is downloading Maven and installing Java. Watch the terminal logs. When you see exactly `Started HisabKitabApplication in X seconds`, look at the top left of the dashboard. Copy your new API URL (e.g., `https://hisab-kitab-api.onrender.com`).

---

## Stage 4: Frontend Hosting (Vercel.com)
Vercel is the industry leader for hosting React applications.

1. Log into **[Vercel.com](https://vercel.com/)** using your GitHub.
2. Click **Add New -> Project**.
3. Import your `HisabKitab---Advanced-Finance-Loan-Management` GitHub repository.
4. **Configure the Project:**
   * **Framework Preset:** Vite (Vercel should auto-detect this).
   * **Root Directory:** Click "Edit" and select `frontend`. *(<- Incredibly important)*
5. **Set Environment Variables:**
   Expand the Environment Variables tab.
   * **Name:** `VITE_API_URL`
   * **Value:** `[PASTE_YOUR_RENDER_URL_HERE]/api` *(Example: `https://hisab-kitab-api.onrender.com/api`)*
6. Click **Deploy**.

Vercel usually takes less than 60 seconds to build. Once complete, click "Continue to Dashboard" and copy your live frontend domain (e.g., `https://hisabkitab-frontend.vercel.app`).

---

## Stage 5: The Final Handshake (CORS Shield)
Right now, your Backend is alive on Render, and your Frontend is alive on Vercel. However, if you open Vercel and try to log in, you will get a massive red `Network Error`.

**Why?** Browsers have a security feature called **CORS (Cross-Origin Resource Sharing)**. Because `vercel.app` is a different domain than `onrender.com`, the backend will reject the traffic as a suspected hacker attack. 

We must explicitly invite the Vercel domain into the Render backend.

1. Go back to your **Render.com Backend Dashboard**.
2. Go to the **Environment** tab on the left.
3. Find your empty `ALLOWED_ORIGINS` variable.
4. **Paste your Vercel URL:** `https://hisabkitab-frontend.vercel.app` *(Make absolutely sure there is NO trailing slash `/` at the end of the URL).*
5. Click **Save Changes**.

Render will automatically restart the Java backend with the new security list. Wait 2 minutes for it to boot back up.

---

🎉 **Congratulations** 🎉
Open your Vercel URL on your phone or laptop. You have successfully designed, engineered, secured, and deployed an enterprise-grade full-stack financial application to the worldwide web, completely for free.
