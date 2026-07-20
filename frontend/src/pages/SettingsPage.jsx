import React, { useState } from 'react';
import { Card, CardContent } from '../components/ui/Card';
import NotificationToggle from '../features/settings/NotificationToggle';

const SettingsPage = () => {
  const [preferences, setPreferences] = useState({
    emailMatchInvites: true,
    emailPromotions: false,
    pushReminders: true,
    pushUpdates: true,
    smsAlerts: false
  });

  const handleToggle = (key, value) => {
    setPreferences(prev => ({ ...prev, [key]: value }));
  };

  return (
    <div className="p-margin-mobile md:p-margin-desktop animate-fade-in max-w-4xl mx-auto pb-24 md:pb-8">
      <div className="mb-8 border-b border-outline-variant/30 pb-6">
        <h1 className="font-headline-lg text-headline-lg text-on-surface">Settings</h1>
        <p className="text-on-surface-variant mt-2">Manage your account and notification preferences.</p>
      </div>

      <div className="space-y-8">
        <section>
          <h2 className="font-headline-md text-lg text-on-surface mb-4">Email Notifications</h2>
          <Card className="overflow-hidden border border-outline-variant/30">
            <div className="divide-y divide-outline-variant/30">
              <NotificationToggle 
                label="Match & Team Invites" 
                description="Receive emails when someone invites you to a team or match."
                enabled={preferences.emailMatchInvites}
                onChange={(val) => handleToggle('emailMatchInvites', val)}
              />
              <NotificationToggle 
                label="Promotional Offers" 
                description="Receive emails about discounts and special events."
                enabled={preferences.emailPromotions}
                onChange={(val) => handleToggle('emailPromotions', val)}
              />
            </div>
          </Card>
        </section>

        <section>
          <h2 className="font-headline-md text-lg text-on-surface mb-4">Push Notifications</h2>
          <Card className="overflow-hidden border border-outline-variant/30">
            <div className="divide-y divide-outline-variant/30">
              <NotificationToggle 
                label="Booking Reminders" 
                description="Get notified 1 hour before your booked slot starts."
                enabled={preferences.pushReminders}
                onChange={(val) => handleToggle('pushReminders', val)}
              />
              <NotificationToggle 
                label="Platform Updates" 
                description="Get notified about major new features."
                enabled={preferences.pushUpdates}
                onChange={(val) => handleToggle('pushUpdates', val)}
              />
            </div>
          </Card>
        </section>

        <section>
          <h2 className="font-headline-md text-lg text-on-surface mb-4">SMS Alerts</h2>
          <Card className="overflow-hidden border border-outline-variant/30">
            <div className="divide-y divide-outline-variant/30">
              <NotificationToggle 
                label="Critical Alerts" 
                description="Receive text messages for cancellations or urgent updates."
                enabled={preferences.smsAlerts}
                onChange={(val) => handleToggle('smsAlerts', val)}
              />
            </div>
          </Card>
        </section>
        
        <div className="flex justify-end pt-4">
          <button className="bg-primary hover:bg-primary-dark text-white px-6 py-2.5 rounded-lg font-label-md transition-colors shadow-sm">
            Save Changes
          </button>
        </div>
      </div>
    </div>
  );
};

export default SettingsPage;
