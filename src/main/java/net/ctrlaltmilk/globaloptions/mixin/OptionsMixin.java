/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.ctrlaltmilk.globaloptions.mixin;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.JsonOps;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.function.Function;

import static net.ctrlaltmilk.globaloptions.GlobalOptions.CONFIG;
import static net.ctrlaltmilk.globaloptions.GlobalOptions.LOGGER;
import static net.ctrlaltmilk.globaloptions.GlobalOptions.optionsFile;

@Mixin(Options.class)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class OptionsMixin {
    @Shadow static @Final Gson GSON;
    @Shadow private static @Final Splitter OPTION_SPLITTER;

    @Shadow protected abstract void processOptions(Options.FieldAccess p_168428_);

    @Inject(method = "load(Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;dataFix(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;"))
    private void loadMixin(boolean limited, CallbackInfo ci, @Local CompoundTag tag) {
        try (BufferedReader reader = Files.newReader(optionsFile(), Charsets.UTF_8)) {
            reader.lines().forEach(line -> {
                try {
                    Iterator<String> i = OPTION_SPLITTER.split(line).iterator();

                    String optionName = i.next();
                    if (globaloptions$optionInConfig(optionName)) {
                        tag.putString(optionName, i.next());
                    }
                } catch (Exception e) {
                    LOGGER.warn("Skipping bad option: {}", line);
                }
            });
        } catch (Exception e) {
            LOGGER.error("Failed to load options", e);
        }
    }

    @Inject(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;broadcastOptions()V"))
    private void saveMixin(CallbackInfo ci) {
        try (PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(optionsFile())))) {

            processOptions(new Options.FieldAccess() {
                private void writeName(String name) {
                    printWriter.print(name);
                    printWriter.print(':');
                }

                @Override
                public int process(String name, int value) {
                    if (globaloptions$optionInConfig(name)) {
                        writeName(name);
                        printWriter.println(value);
                    }

                    return value;
                }

                @Override
                public boolean process(String name, boolean value) {
                    if (globaloptions$optionInConfig(name)) {
                        writeName(name);
                        printWriter.println(value);
                    }

                    return value;
                }

                @Override
                public String process(String name, String value) {
                    if (globaloptions$optionInConfig(name)) {
                        writeName(name);
                        printWriter.println(value);
                    }

                    return value;
                }

                @Override
                public float process(String name, float value) {
                    if (globaloptions$optionInConfig(name)) {
                        writeName(name);
                        printWriter.println(value);
                    }

                    return value;
                }

                @Override
                public <T> T process(String name, T value, Function<String, T> decoder, Function<T, String> encoder) {
                    if (globaloptions$optionInConfig(name)) {
                        writeName(name);
                        printWriter.println(encoder.apply(value));
                    }

                    return value;
                }

                @Override
                public <T> void process(String name, OptionInstance<T> option) {
                    if (globaloptions$optionInConfig(name)) {
                        option.codec()
                                .encodeStart(JsonOps.INSTANCE, option.get())
                                .ifError(err -> LOGGER.error("Error saving option {}: {}", name, err))
                                .ifSuccess(json -> {
                                    writeName(name);
                                    printWriter.println(GSON.toJson(json));
                                });
                    }
                }
            });
        } catch (Exception e) {
            LOGGER.error("Failed to save options", e);
        }
    }

    @Unique
    private static boolean globaloptions$optionInConfig(String optionName) {
        boolean ret = false;

        for (String line : CONFIG.get().allowedOptions) {
            if (globaloptions$matchWithWildcard(line, optionName)) {
                ret = true;
                break;
            }
        }

        if (!ret) {
            // we can't disallow something that isn't allowed, bail out early
            return false;
        }

        for (String line : CONFIG.get().disallowedOptions) {
            if (globaloptions$matchWithWildcard(line, optionName)) {
                ret = false;
                break;
            }
        }

        return ret;
    }

    @Unique
    private static boolean globaloptions$matchWithWildcard(String pattern, String text) {
        int starPos = pattern.indexOf('*');

        if (starPos < 0) {
            // non-wildcard, must match exactly
            return text.equals(pattern);
        } else {
            // wildcard, just match prefix
            return text.startsWith(pattern.substring(0, starPos));
        }
    }
}
